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
    public void consumeMaterialByLog(PcbPerformanceLog log) {
        int goodQty = Math.max(0, log.getTotalModules());
        int productId = log.getProductId();
        int warehouseId = log.getWarehouseId();
        int logId = log.getLogId();

        // 1️⃣ Lấy RunID đang chạy
        Integer runId = null;
        try {
            String getRunIdSql = """
            SELECT TOP 1 r.RunID
            FROM ModelLineRuns r
            JOIN ModelLines ml ON r.ModelLineID = ml.ModelLineID
            WHERE ml.ProductID = ? AND ml.WarehouseID = ? AND r.Status = 'Running'
        """;
            runId = jdbcTemplate.queryForObject(getRunIdSql, Integer.class, productId, warehouseId);
        } catch (Exception ignored) {}

        if (runId == null) {
            String warnMsg = String.format(
                    "⚠️ Không có phiên (Run) nào đang RUNNING cho ProductID=%d tại WarehouseID=%d → Bỏ qua trừ liệu.",
                    productId, warehouseId
            );

            // ⚠️ Ghi ra console
            System.err.println("[consumeMaterialByLog] " + warnMsg);

            // ⚠️ Báo lên UI log nếu controller có callback (ví dụ: appendLog)
            try {
                // Nếu logService hoặc tiêu chuẩn hóa callback
                if (log.getLogFileName() != null) {
                    jdbcTemplate.update("""
                    INSERT INTO MaterialConsumeDetailLog 
                    (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt, SourceLogId, Note)
                    VALUES (NULL, ?, NULL, 0, GETDATE(), ?, ?)
                """, log.getCreatedAt().toLocalDate(), logId, warnMsg);
                }
            } catch (Exception e) {
                System.err.println("[consumeMaterialByLog] ❌ Không thể ghi note cảnh báo: " + e.getMessage());
            }

            return; // ✅ Dừng tại đây, không trừ vật tư
        }

        // 2️⃣ Lấy BOM
        String getBomSql = "SELECT SapPN, Quantity FROM ProductBOM WHERE ProductID = ?";
        List<Map<String, Object>> bomList = jdbcTemplate.queryForList(getBomSql, productId);
        if (bomList.isEmpty()) return;

        // 3️⃣ Lấy feeders thuộc line đó
        String getFeedersSql = """
        SELECT f.FeederID, f.SapCode
        FROM Feeders f
        JOIN ModelLines ml ON f.ModelLineID = ml.ModelLineID
        WHERE ml.ProductID = ? AND ml.WarehouseID = ?
    """;
        List<Map<String, Object>> feeders = jdbcTemplate.queryForList(getFeedersSql, productId, warehouseId);
        Map<String, List<Map<String, Object>>> feedersBySap = feeders.stream()
                .collect(Collectors.groupingBy(f -> ((String) f.get("SapCode")).trim().toUpperCase()));

        // 4️⃣ Duyệt BOM
        for (Map<String, Object> bom : bomList) {
            String sapCode = ((String) bom.get("SapPN")).trim().toUpperCase();
            double qtyPerBoard = ((Number) bom.get("Quantity")).doubleValue();
            int needQty = (int) Math.ceil(qtyPerBoard * goodQty);
            if (needQty <= 0) continue;

            List<Map<String, Object>> sapFeeders = feedersBySap.getOrDefault(sapCode, List.of());
            if (sapFeeders.isEmpty()) {
                System.err.printf("[consumeMaterialByLog] ⚠️ Không có feeder gắn SAP %s%n", sapCode);
                insertShortageNote(logId, log.getCreatedAt(), sapCode, needQty);
                continue;
            }

            for (Map<String, Object> feeder : sapFeeders) {
                if (needQty <= 0) break;

                int feederId = (int) feeder.get("FeederID");

                // ✅ Chỉ lấy cuộn thuộc RunID đang chạy
                String getRollsSql = """
                SELECT m.MaterialID, m.Quantity
                FROM FeederAssignmentMaterials fam
                JOIN Materials m ON fam.MaterialID = m.MaterialID
                JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
                WHERE fa.FeederID = ?
                  AND fa.RunID = ?       -- ✅ chỉ run hiện tại
                  AND fam.IsActive = 1
                ORDER BY fam.AttachedAt ASC
            """;

                List<Map<String, Object>> rolls = jdbcTemplate.queryForList(getRollsSql, feederId, runId);
                if (rolls.isEmpty()) {
                    System.err.printf("[consumeMaterialByLog] ⚠️ Feeder %d chưa gắn cuộn cho SAP %s trong run #%d%n",
                            feederId, sapCode, runId);
                    continue;
                }

                for (Map<String, Object> roll : rolls) {
                    if (needQty <= 0) break;

                    int materialId = (int) roll.get("MaterialID");
                    int availableQty = (int) roll.get("Quantity");
                    int consumeNow = Math.min(availableQty, needQty);

                    int updated = jdbcTemplate.update(
                            "UPDATE Materials SET Quantity = Quantity - ? WHERE MaterialID = ? AND Quantity >= ?",
                            consumeNow, materialId, consumeNow
                    );
                    if (updated == 0) continue;

                    jdbcTemplate.update("""
                    INSERT INTO MaterialConsumeDetailLog 
                    (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt, SourceLogId)
                    VALUES (NULL, ?, ?, ?, GETDATE(), ?)
                """, log.getCreatedAt().toLocalDate(), materialId, consumeNow, logId);

                    needQty -= consumeNow;
                }
            }

            // 5️⃣ Nếu vẫn thiếu → log note
            if (needQty > 0) {
                insertShortageNote(logId, log.getCreatedAt(), sapCode, needQty);
            }
        }

        System.out.printf("[consumeMaterialByLog] ✅ Đã trừ xong liệu cho log #%d (GOOD=%d, RunID=%d)%n",
                logId, goodQty, runId);
    }



    // 🧩 Ghi chú thiếu vật tư
    private void insertShortageNote(int logId, LocalDateTime date, String sapCode, int missingQty) {
        String noteSql = """
        INSERT INTO MaterialConsumeDetailLog 
        (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt, SourceLogId, Note)
        VALUES (NULL, ?, NULL, 0, GETDATE(), ?, ?)
    """;
        String note = "Thiếu " + missingQty + " pcs cho SAP " + sapCode;
        jdbcTemplate.update(noteSql, date.toLocalDate(), logId, note);
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
                   m.Maker,                     -- 🆕 thêm Maker
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
