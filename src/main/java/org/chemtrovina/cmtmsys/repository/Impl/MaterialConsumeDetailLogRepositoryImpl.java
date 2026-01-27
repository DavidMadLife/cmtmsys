package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.MaterialUsage;
import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialConsumeDetailLogRowMapper;
import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeDetailLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MaterialConsumeDetailLogRepositoryImpl implements MaterialConsumeDetailLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialConsumeDetailLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(MaterialConsumeDetailLog log) {
        String sql = """
            INSERT INTO MaterialConsumeDetailLog 
            (PlanItemID, RunDate, MaterialID, ConsumedQty, SourceLogId, CreatedAt)
            VALUES (?, ?, ?, ?, ?, GETDATE())
        """;
        jdbcTemplate.update(sql,
                log.getPlanItemId(),
                log.getRunDate(),
                log.getMaterialId(),
                log.getConsumedQty(),
                log.getSourceLogId()
        );
    }

    @Override
    public List<MaterialConsumeDetailLog> findByPlanItemAndDate(int planItemId, LocalDate runDate) {
        String sql = "SELECT * FROM MaterialConsumeDetailLog WHERE PlanItemID = ? AND RunDate = ?";
        return jdbcTemplate.query(sql, new MaterialConsumeDetailLogRowMapper(), planItemId, runDate);
    }

    @Override
    public void deleteByPlanItemAndDate(int planItemId, LocalDate runDate) {
        String sql = "DELETE FROM MaterialConsumeDetailLog WHERE PlanItemID = ? AND RunDate = ?";
        jdbcTemplate.update(sql, planItemId, runDate);
    }

    @Override
    public List<MaterialConsumeDetailLog> findBySourceLogId(int sourceLogId) {
        String sql = "SELECT * FROM MaterialConsumeDetailLog WHERE SourceLogId = ?";
        return jdbcTemplate.query(sql, new MaterialConsumeDetailLogRowMapper(), sourceLogId);
    }

    @Override
    public List<String> consumeByAoiLog(PcbPerformanceLog log) {

        List<String> shortages = new ArrayList<>();

        int goodQty = Math.max(0, log.getTotalModules());
        int productId = log.getProductId();
        int warehouseId = log.getWarehouseId();
        int logId = log.getLogId();

        // 1️⃣ Lấy RunID đang chạy
        Integer runId = null;
        try {
            runId = jdbcTemplate.queryForObject("""
            SELECT TOP 1 r.RunID
            FROM ModelLineRuns r
            JOIN ModelLines ml ON r.ModelLineID = ml.ModelLineID
            WHERE ml.ProductID = ? AND ml.WarehouseID = ? AND r.Status = 'Running'
        """, Integer.class, productId, warehouseId);
        } catch (Exception ignored) {}

        if (runId == null) {
            shortages.add("⚠️ Không có RUNNING cho Product " + productId + " tại Warehouse " + warehouseId);
            return shortages;
        }

        // 2️⃣ Lấy feeders của line → Dùng feeder.qty thay cho BOM
        List<Map<String, Object>> feeders = jdbcTemplate.queryForList("""
        SELECT f.FeederID, f.SapCode, f.Qty
        FROM Feeders f
        JOIN ModelLines ml ON f.ModelLineID = ml.ModelLineID
        WHERE ml.ProductID = ? AND ml.WarehouseID = ?
    """, productId, warehouseId);

        if (feeders.isEmpty()) {
            shortages.add("❌ Không có FEEDER nào được khai báo cho line");
            return shortages;
        }



        // 3️⃣ Duyệt từng FEEDER để trừ liệu
        int feederNoRollCount = 0;
        List<String> feederNoRollSapCodes = new ArrayList<>();

        for (Map<String, Object> feeder : feeders) {

            int feederId = (int) feeder.get("FeederID");
            int qtyPerBoard = (int) feeder.get("Qty");     // dùng số lượng của feeder
            String sapCode = ((String) feeder.get("SapCode")).trim().toUpperCase();

            int needQty = qtyPerBoard;
            if (qtyPerBoard <= 0) continue;


            // 4️⃣ Lấy danh sách cuộn đang active gắn vào feeder
            List<Map<String, Object>> rolls = jdbcTemplate.queryForList("""
            SELECT m.MaterialID, m.Quantity
            FROM FeederAssignmentMaterials fam
            JOIN Materials m ON fam.MaterialID = m.MaterialID
            JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
            WHERE fa.FeederID = ?
              AND fa.RunID = ?
              AND fam.IsActive = 1
            ORDER BY fam.AttachedAt ASC
        """, feederId, runId);

            if (rolls.isEmpty()) {
                feederNoRollCount++;
                feederNoRollSapCodes.add(sapCode);
                continue;
            }


            // 5️⃣ Trừ liệu từ các cuộn theo FIFO
            for (Map<String, Object> roll : rolls) {

                if (needQty <= 0) break;

                int materialId = (int) roll.get("MaterialID");
                int availableQty = (int) roll.get("Quantity");

                int consumeNow = Math.min(availableQty, needQty);

                // Trừ trong DB
                int updated = jdbcTemplate.update(
                        "UPDATE Materials SET Quantity = Quantity - ? WHERE MaterialID = ? AND Quantity >= ?",
                        consumeNow, materialId, consumeNow
                );

                if (updated == 0) continue;  // cuộn này hết

                // Nếu cuộn cạn → disable
                if (consumeNow == availableQty) {
                    jdbcTemplate.update("UPDATE FeederAssignmentMaterials SET IsActive = 0 WHERE MaterialID = ?", materialId);
                }

                // Ghi log tiêu thụ
                jdbcTemplate.update("""
                INSERT INTO MaterialConsumeDetailLog
                (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt, SourceLogId)
                VALUES (NULL, ?, ?, ?, GETDATE(), ?)
            """, log.getCreatedAt().toLocalDate(), materialId, consumeNow, logId);

                needQty -= consumeNow;
            }

           /* // 6️⃣ Nếu vẫn thiếu → báo cảnh báo (không ghi DB)
            if (needQty > 0) {
                shortages.add("❌ Thiếu SAP " + sapCode + " → còn thiếu " + needQty + " pcs");
            }*/
        }

        if (feederNoRollCount > 0) {
            shortages.add(
                    "⚠️ Có " + feederNoRollCount +
                            " feeder chưa gắn cuộn (SAP: " +
                            String.join(", ", feederNoRollSapCodes) + ")"
            );
        }


        System.out.printf("[consumeMaterial] ✔ DONE log #%d (GOOD=%d)\n", logId, goodQty);

        return shortages;
    }



    // 🧩 Ghi chú thiếu vật tư
    private void insertShortageNote(int logId, LocalDateTime date, String sapCode, int missingQty) {

        String note = "Thiếu " + missingQty + " pcs cho SAP " + sapCode;

        jdbcTemplate.update("""
        INSERT INTO MaterialConsumeDetailLog 
        (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt, SourceLogId, Note)
        VALUES (NULL, ?, 0, 0, GETDATE(), ?, ?)
    """, date.toLocalDate(), logId, note);
    }



    private static final RowMapper<MaterialUsage> USAGE_MAPPER = (rs, i) -> new MaterialUsage(
            rs.getString("SapCode"),
            rs.getString("RollCode"),
            rs.getInt("QuantityUsed"),
            rs.getString("WarehouseName"),
            rs.getString("Spec"),
            rs.getString("Lot"),
            rs.getString("Maker"),
            rs.getTimestamp("Created").toLocalDateTime()
    );

    @Override
    public List<MaterialUsage> findUsageBySourceLogId(int sourceLogId) {
        String sql = """
            SELECT m.SapCode,
                   m.RollCode,
                   d.ConsumedQty AS QuantityUsed,
                   w.Name AS WarehouseName,
                   m.Spec,
                   m.Lot,
                   m.Maker,                     -- 🆕 thêm Maker
                   d.CreatedAt AS Created
            FROM MaterialConsumeDetailLog d
            JOIN Materials m ON m.MaterialID = d.MaterialID
            JOIN Warehouses w ON w.WarehouseID = m.WarehouseID
            WHERE d.SourceLogId = ?
              AND d.MaterialID IS NOT NULL
            ORDER BY d.CreatedAt DESC
        """;
        return jdbcTemplate.query(sql, USAGE_MAPPER, sourceLogId);
    }

    @Override
    public List<MaterialUsage> findUsageByCarrierId(String carrierId) {
        String sql = """
            SELECT m.SapCode,
                   m.RollCode,
                   d.ConsumedQty AS QuantityUsed,
                   w.Name AS WarehouseName,
                   m.Spec,
                   m.Lot,
                   m.Maker,                    
                   d.CreatedAt AS Created
            FROM MaterialConsumeDetailLog d
            JOIN Materials m ON m.MaterialID = d.MaterialID
            JOIN Warehouses w ON w.WarehouseID = m.WarehouseID
            JOIN PcbPerformanceLog p ON p.LogID = d.SourceLogId
            WHERE p.CarrierID = ?
              AND d.MaterialID IS NOT NULL
            ORDER BY d.CreatedAt DESC
        """;
        return jdbcTemplate.query(sql, USAGE_MAPPER, carrierId);
    }

    @Override
    public List<MaterialUsage> searchUsage(String modelCode, ModelType modelType,
                                           LocalDateTime from, LocalDateTime to) {
        StringBuilder sb = new StringBuilder("""
            SELECT m.SapCode,
                   m.RollCode,
                   d.ConsumedQty AS QuantityUsed,
                   w.Name AS WarehouseName,
                   m.Spec,
                   m.Lot,
                   m.Maker,                     -- 🆕 thêm Maker
                   d.CreatedAt AS Created
            FROM MaterialConsumeDetailLog d
            JOIN Materials m ON m.MaterialID = d.MaterialID
            JOIN Warehouses w ON w.WarehouseID = m.WarehouseID
            JOIN PcbPerformanceLog p ON p.LogID = d.SourceLogId
            JOIN Products pr ON pr.ProductID = p.ProductID
            WHERE d.MaterialID IS NOT NULL
        """);

        List<Object> args = new ArrayList<>();

        if (modelCode != null && !modelCode.isBlank()) {
            sb.append(" AND pr.ProductCode = ? ");
            args.add(modelCode);
        }
        if (modelType != null) {
            sb.append(" AND pr.ModelType = ? ");
            args.add(modelType.name());
        }
        if (from != null) {
            sb.append(" AND d.CreatedAt >= ? ");
            args.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sb.append(" AND d.CreatedAt <= ? ");
            args.add(Timestamp.valueOf(to));
        }
        sb.append(" ORDER BY d.CreatedAt DESC");

        return jdbcTemplate.query(sb.toString(), USAGE_MAPPER, args.toArray());
    }
}
