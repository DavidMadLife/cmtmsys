package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductionPlanDailyRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanDailyRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.time.temporal.WeekFields;
import java.util.stream.Collectors;

@Repository
public class ProductionPlanDailyRepositoryImpl implements ProductionPlanDailyRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductionPlanDailyRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ProductionPlanDaily daily) {
        String sql = "INSERT INTO ProductionPlanDaily (PlanItemID, RunDate, Quantity, ActualQuantity) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, daily.getPlanItemID(), daily.getRunDate(), daily.getQuantity(), daily.getActualQuantity());
    }

    @Override
    public void update(ProductionPlanDaily daily) {
        String sql = "UPDATE ProductionPlanDaily SET Quantity = ?, ActualQuantity = ? WHERE DailyID = ?";
        jdbcTemplate.update(sql, daily.getQuantity(), daily.getActualQuantity(), daily.getDailyID());
    }

    @Override
    public void deleteById(int dailyId) {
        jdbcTemplate.update("DELETE FROM ProductionPlanDaily WHERE DailyID = ?", dailyId);
    }

    @Override
    public List<ProductionPlanDaily> findByPlanItemId(int planItemId) {
        String sql = "SELECT * FROM ProductionPlanDaily WHERE PlanItemID = ?";
        return jdbcTemplate.query(sql, new ProductionPlanDailyRowMapper(), planItemId);
    }

    @Override
    public List<ProductionPlanDaily> findByPlanItemIdAndDateRange(int planItemId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM ProductionPlanDaily WHERE PlanItemID = ? AND RunDate BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new ProductionPlanDailyRowMapper(), planItemId, startDate, endDate);
    }

    @Override
    public void updateDailyPlanAndActual(int planItemId, String runDate, int planQty, int actualQty) {
        String sql = """
            MERGE INTO ProductionPlanDaily AS target
            USING (SELECT ? AS PlanItemID, ? AS RunDate) AS source
            ON target.PlanItemID = source.PlanItemID AND target.RunDate = source.RunDate
            WHEN MATCHED THEN
                UPDATE SET Quantity = ?, ActualQuantity = ?
            WHEN NOT MATCHED THEN
                INSERT (PlanItemID, RunDate, Quantity, ActualQuantity)
                VALUES (?, ?, ?, ?);
        """;

        jdbcTemplate.update(sql,
                planItemId, runDate, planQty, actualQty,
                planItemId, runDate, planQty, actualQty
        );
    }

    @Override
    public List<DailyPlanRowDto> getDailyPlansByLineAndWeek(String lineName, int weekNo, int year) {
        String sql = """
        SELECT 
            ppi.PlanItemID, 
            p.ProductCode, 
            p.ProductCode AS SapCode, 
            p.ModelType,
            0 AS Stock,  -- Nếu muốn bỏ luôn stock thì có thể giữ 0 ở đây

            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day1,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day2,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day3,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day4,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day5,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day6,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.Quantity ELSE 0 END) AS Day7,

            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A1,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A2,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A3,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A4,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A5,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A6,
            MAX(CASE WHEN ppd.RunDate = ? THEN ppd.ActualQuantity ELSE 0 END) AS A7

        FROM ProductionPlans pp
        JOIN Warehouses w ON pp.LineWarehouseID = w.WarehouseID
        JOIN ProductionPlanItems ppi ON pp.PlanID = ppi.PlanID
        JOIN Products p ON ppi.ProductID = p.ProductID
        LEFT JOIN ProductionPlanDaily ppd ON ppi.PlanItemID = ppd.PlanItemID

        WHERE w.Name = ? AND pp.WeekNo = ? AND pp.Year = ?
        GROUP BY ppi.PlanItemID, p.ProductCode, p.ModelType
    """;

        LocalDate monday = getStartOfWeek(weekNo, year);

        Object[] params = new Object[]{
                monday.plusDays(0), monday.plusDays(1), monday.plusDays(2), monday.plusDays(3),
                monday.plusDays(4), monday.plusDays(5), monday.plusDays(6),
                monday.plusDays(0), monday.plusDays(1), monday.plusDays(2), monday.plusDays(3),
                monday.plusDays(4), monday.plusDays(5), monday.plusDays(6),
                lineName, weekNo, year
        };

        return jdbcTemplate.query(sql, params, new DailyPlanRowDtoMapper());
    }


    private LocalDate getStartOfWeek(int weekNo, int year) {
        return LocalDate.of(year, 1, 1)
                .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), weekNo)
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
    }

    public int getTotalDailyPlanByPlanItemId(int planItemId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM ProductionPlanDaily WHERE planItemID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, planItemId);
    }

    // Lấy từ bảng gốc kế hoạch tuần
    @Override
    public int getPlannedWeeklyQuantityByPlanItemId(int planItemId) {
        String sql = "SELECT PlannedQuantity FROM ProductionPlanItems WHERE PlanItemID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, planItemId);
    }

    @Override
    public void updateDailyPlan(int planItemId, LocalDate runDate, int planQty) {
        String sql = """
        MERGE INTO ProductionPlanDaily AS target
        USING (SELECT ? AS planItemId, ? AS runDate) AS src
        ON target.PlanItemID = src.planItemId AND target.RunDate = src.runDate
        WHEN MATCHED THEN
            UPDATE SET Quantity = ?
        WHEN NOT MATCHED THEN
            INSERT (PlanItemID, RunDate, Quantity, ActualQuantity, CreatedAt)
            VALUES (?, ?, ?, 0, GETDATE());
    """;

        jdbcTemplate.update(sql,
                planItemId, runDate,
                planQty,
                planItemId, runDate, planQty
        );
    }

    public List<DailyPlanRowDto> getDailyPlanViewForAllLines(int week, int year) {
        String sql = "SELECT * FROM ... WHERE week = ? AND year = ?"; // bỏ lọc theo line
        return jdbcTemplate.query(sql, new Object[]{week, year}, new DailyPlanRowDtoMapper());
    }


    @Override
    public void updateActual(int planItemId, LocalDate runDate, int actualQty) {
        String sql = """
        MERGE INTO ProductionPlanDaily AS target
        USING (SELECT ? AS PlanItemID, ? AS RunDate) AS source
        ON target.PlanItemID = source.PlanItemID AND target.RunDate = source.RunDate
        WHEN MATCHED THEN
            UPDATE SET ActualQuantity = ?
        WHEN NOT MATCHED THEN
            INSERT (PlanItemID, RunDate, Quantity, ActualQuantity, CreatedAt)
            VALUES (?, ?, 0, ?, GETDATE());
    """;

        jdbcTemplate.update(sql,
                planItemId, runDate,
                actualQty,
                planItemId, runDate, actualQty
        );
    }

    @Override
    public void consumeMaterialByActual(int planItemId, LocalDate runDate, int actualQty) {
        // 1. Lấy ProductID và PlanID từ PlanItem
        String getProductSql = "SELECT ProductID, PlanID FROM ProductionPlanItems WHERE PlanItemID = ?";
        Map<String, Object> productAndPlan = jdbcTemplate.queryForMap(getProductSql, planItemId);

        int productId = (int) productAndPlan.get("ProductID");
        int planId = (int) productAndPlan.get("PlanID");

        // 2. Lấy WarehouseID từ ProductionPlans
        String getWarehouseSql = "SELECT LineWarehouseID FROM ProductionPlans WHERE PlanID = ?";
        int warehouseId = jdbcTemplate.queryForObject(getWarehouseSql, Integer.class, planId);

        // 3. Tìm RunID đang chạy
        String getRunIdSql = """
        SELECT r.RunID
        FROM ModelLineRuns r
        JOIN ModelLines ml ON r.ModelLineID = ml.ModelLineID
        WHERE ml.ProductID = ? AND ml.WarehouseID = ? AND r.Status = 'Running'
    """;
        Integer runId = jdbcTemplate.queryForObject(getRunIdSql, Integer.class, productId, warehouseId);
        if (runId == null) {
            throw new RuntimeException("Không tìm thấy phiên đang chạy để trừ liệu.");
        }

        // 4. Lấy danh sách Feeder (CẦN LẤY THÊM cột Qty)
        String getFeedersSql = """
        SELECT f.FeederID, f.SapCode, f.Qty
        FROM Feeders f
        JOIN ModelLines ml ON f.ModelLineID = ml.ModelLineID
        WHERE ml.ProductID = ? AND ml.WarehouseID = ?
    """;
        List<Map<String, Object>> feeders = jdbcTemplate.queryForList(getFeedersSql, productId, warehouseId);

        for (Map<String, Object> feeder : feeders) {
            int feederId = (int) feeder.get("FeederID");
            String sapCode = (String) feeder.get("SapCode");
            int feederQty = (int) feeder.get("Qty");  // SỐ LIỆU TIÊU THỤ MỖI SẢN PHẨM

            int qtyToConsume = actualQty * feederQty;

            // 5. Lấy cuộn theo FIFO
            String getMaterialsSql = """
            SELECT m.MaterialID, m.Quantity
            FROM FeederAssignmentMaterials fam
            JOIN Materials m ON fam.MaterialID = m.MaterialID
            JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
            WHERE fa.FeederID = ? AND fa.RunID = ? AND fam.IsActive = 1
            ORDER BY fam.AttachedAt ASC
        """;
            List<Map<String, Object>> assignedRolls = jdbcTemplate.queryForList(getMaterialsSql, feederId, runId);

            for (Map<String, Object> roll : assignedRolls) {
                if (qtyToConsume <= 0) break;

                int materialId = (int) roll.get("MaterialID");
                int availableQty = (int) roll.get("Quantity");

                int consumeNow = Math.min(availableQty, qtyToConsume);

                String updateMatSql = """
                UPDATE Materials 
                SET Quantity = Quantity - ? 
                WHERE MaterialID = ? AND Quantity >= ?
            """;

                int updated = jdbcTemplate.update(updateMatSql, consumeNow, materialId, consumeNow);
                if (updated == 0) {
                    throw new RuntimeException("Không đủ vật liệu ở cuộn " + materialId + " để trừ.");
                }

                qtyToConsume -= consumeNow;
            }

            if (qtyToConsume > 0) {
                throw new RuntimeException("Không đủ liệu để trừ cho SAP " + sapCode + " theo FIFO trong phiên đang chạy.");
            }
        }
    }




    @Override
    public int getActualQuantity(int planItemId, LocalDate runDate) {
        String sql = "SELECT COALESCE(ActualQuantity, 0) FROM ProductionPlanDaily WHERE PlanItemID = ? AND RunDate = ?";
        List<Integer> result = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt(1),
                planItemId, runDate);

        return result.isEmpty() ? 0 : result.get(0);
    }
    @Override
    public ProductionPlanDaily findByPlanItemIdAndRunDate(int planItemId, LocalDate runDate) {
        String sql = "SELECT * FROM ProductionPlanDaily WHERE PlanItemID = ? AND RunDate = ?";
        List<ProductionPlanDaily> list = jdbcTemplate.query(sql, new ProductionPlanDailyRowMapper(), planItemId, runDate);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public ProductionPlanDaily findByModelCodeAndLineAndDate(String modelCode, String lineName, LocalDate runDate) {
        String sql = """
    SELECT d.*
    FROM ProductionPlanDaily d
    JOIN ProductionPlanItems i ON d.PlanItemID = i.PlanItemID
    JOIN ProductionPlans pp ON i.PlanID = pp.PlanID
    JOIN Warehouses w ON pp.LineWarehouseID = w.WarehouseID
    JOIN Products p ON i.ProductID = p.ProductID
    WHERE p.ProductCode = ? AND w.Name = ? AND d.RunDate = ?
""";


        return jdbcTemplate.query(sql, new Object[]{modelCode, lineName, runDate}, rs -> {
            if (rs.next()) {
                ProductionPlanDaily daily = new ProductionPlanDaily();
                daily.setDailyID(rs.getInt("DailyId"));
                daily.setPlanItemID(rs.getInt("PlanItemId"));
                daily.setRunDate(rs.getDate("RunDate").toLocalDate());
                daily.setQuantity(rs.getInt("Quantity"));
                daily.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                return daily;
            }
            return null;
        });
    }

    @Override
    public Map<String, ProductionPlanDaily> findByModelLineAndDates(Set<String> keys) {
        if (keys == null || keys.isEmpty()) return new HashMap<>();

        String sql = """
        SELECT d.*, p.ProductCode, w.Name AS WarehouseName
        FROM ProductionPlanDaily d
        JOIN ProductionPlanItems i ON d.PlanItemId = i.PlanItemId
        JOIN Products p ON i.ProductId = p.ProductId
        JOIN ProductionPlans pp ON i.PlanId = pp.PlanId
        JOIN Warehouses w ON pp.LineWarehouseId = w.WarehouseId
        WHERE (p.ProductCode + '|' + w.Name + '|' + FORMAT(d.RunDate, 'yyyy-MM-dd')) IN (%s)
    """;

        // Tạo placeholders ?, ?, ?...
        String placeholders = keys.stream().map(k -> "?").collect(Collectors.joining(", "));
        sql = String.format(sql, placeholders);

        List<Object> params = new ArrayList<>(keys);

        return jdbcTemplate.query(sql, params.toArray(), rs -> {
            Map<String, ProductionPlanDaily> map = new HashMap<>();
            while (rs.next()) {
                ProductionPlanDaily daily = new ProductionPlanDaily();
                daily.setDailyID(rs.getInt("DailyID"));
                daily.setPlanItemID(rs.getInt("PlanItemID"));
                daily.setRunDate(rs.getDate("RunDate").toLocalDate());
                daily.setQuantity(rs.getInt("Quantity"));
                daily.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                daily.setActualQuantity(rs.getInt("ActualQuantity"));

                String model = rs.getString("ProductCode");
                String line = rs.getString("WarehouseName");
                LocalDate date = rs.getDate("RunDate").toLocalDate();

                String key = model + "|" + line + "|" + date;
                map.put(key, daily);
            }
            return map;
        });
    }




    private static class DailyPlanRowDtoMapper implements RowMapper<DailyPlanRowDto> {
        @Override
        public DailyPlanRowDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DailyPlanRowDto(
                    rs.getInt("PlanItemID"),
                    rs.getString("ProductCode"),
                    rs.getString("SapCode"),
                    rs.getInt("Stock"),
                    rs.getInt("Day1"), rs.getInt("Day2"), rs.getInt("Day3"), rs.getInt("Day4"),
                    rs.getInt("Day5"), rs.getInt("Day6"), rs.getInt("Day7"),
                    rs.getInt("A1"), rs.getInt("A2"), rs.getInt("A3"), rs.getInt("A4"),
                    rs.getInt("A5"), rs.getInt("A6"), rs.getInt("A7"),
                    rs.getString("ModelType")   // 👈 truyền thêm tham số này
            );

        }
    }
}
