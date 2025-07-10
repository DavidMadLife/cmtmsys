package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.ProductionPlan;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductionPlanRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ProductionPlanRepositoryImpl implements ProductionPlanRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductionPlanRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ProductionPlan plan) {
        String sql = "INSERT INTO ProductionPlans (LineWarehouseID, WeekNo, Year, FromDate, ToDate) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, plan.getLineWarehouseID(), plan.getWeekNo(), plan.getYear(), plan.getFromDate(), plan.getToDate());
    }

    @Override
    public void update(ProductionPlan plan) {
        String sql = "UPDATE ProductionPlans SET LineWarehouseID = ?, WeekNo = ?, Year = ?, FromDate = ?, ToDate = ? WHERE PlanID = ?";
        jdbcTemplate.update(sql, plan.getLineWarehouseID(), plan.getWeekNo(), plan.getYear(), plan.getFromDate(), plan.getToDate(), plan.getPlanID());
    }

    @Override
    public void deleteById(int planId) {
        jdbcTemplate.update("DELETE FROM ProductionPlans WHERE PlanID = ?", planId);
    }

    @Override
    public ProductionPlan findById(int planId) {
        List<ProductionPlan> result = jdbcTemplate.query("SELECT * FROM ProductionPlans WHERE PlanID = ?", new ProductionPlanRowMapper(), planId);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<ProductionPlan> findAll() {
        return jdbcTemplate.query("SELECT * FROM ProductionPlans", new ProductionPlanRowMapper());
    }

    @Override
    public List<WeeklyPlanDto> searchWeeklyPlans(String line, String model, Integer weekNo, Integer year) {
        String sql = """
        SELECT 
            w.Name AS LineName,
            p.ProductCode,
            pp.WeekNo AS WeekNumber,
            CONVERT(varchar, pp.FromDate, 23) AS FromDate,
            CONVERT(varchar, pp.ToDate, 23) AS ToDate,
            ppi.PlannedQuantity,
            ISNULL(SUM(pdi.ActualQuantity), 0) AS ActualQuantity,
            ppi.PlannedQuantity - ISNULL(SUM(pdi.ActualQuantity), 0) AS DiffQuantity
        FROM ProductionPlans pp
        JOIN Warehouses w ON w.WarehouseID = pp.LineWarehouseID
        JOIN ProductionPlanItems ppi ON ppi.PlanID = pp.PlanID
        JOIN Products p ON p.ProductID = ppi.ProductID
        LEFT JOIN ProductionPlanDaily pdi ON pdi.PlanItemID = ppi.PlanItemID
        WHERE 1 = 1
    """;

        StringBuilder condition = new StringBuilder();
        if (line != null && !line.isBlank()) {
            condition.append(" AND w.Name LIKE ? ");
        }
        if (model != null && !model.isBlank()) {
            condition.append(" AND p.ProductCode LIKE ? ");
        }
        if (weekNo != null) {
            condition.append(" AND pp.WeekNo = ? ");
        }
        if (year != null) {
            condition.append(" AND pp.Year = ? ");
        }

        sql += condition.toString();
        sql += " GROUP BY w.Name, p.ProductCode, pp.WeekNo, pp.FromDate, pp.ToDate, ppi.PlannedQuantity";

        return jdbcTemplate.query(sql, ps -> {
            int i = 1;
            if (line != null && !line.isBlank()) ps.setString(i++, "%" + line + "%");
            if (model != null && !model.isBlank()) ps.setString(i++, "%" + model + "%");
            if (weekNo != null) ps.setInt(i++, weekNo);
            if (year != null) ps.setInt(i++, year);
        }, (rs, rowNum) -> new WeeklyPlanDto(
                rs.getString("LineName"),
                rs.getString("ProductCode"),
                rs.getInt("WeekNumber"),
                rs.getString("FromDate"),
                rs.getString("ToDate"),
                rs.getInt("PlannedQuantity"),
                rs.getInt("ActualQuantity"),
                rs.getInt("DiffQuantity")
        ));
    }


    @Override
    public boolean createWeeklyPlan(String lineName, List<SelectedModelDto> modelList,
                                    LocalDate fromDate, LocalDate toDate, int weekNo, int year) {
        try {
            // Lấy LineWarehouseID từ tên line
            Integer warehouseId = jdbcTemplate.queryForObject(
                    "SELECT WarehouseID FROM Warehouses WHERE Name = ?", Integer.class, lineName);
            if (warehouseId == null) return false;

            // Tạo kế hoạch tuần (ProductionPlans)
            String insertPlanSql = """
            INSERT INTO ProductionPlans (LineWarehouseID, WeekNo, Year, FromDate, ToDate, CreatedAt)
            OUTPUT INSERTED.PlanID
            VALUES (?, ?, ?, ?, ?, GETDATE())
        """;

            Integer planId = jdbcTemplate.queryForObject(insertPlanSql, Integer.class,
                    warehouseId, weekNo, year, fromDate, toDate);
            if (planId == null) return false;

            // Insert từng item sản phẩm (ProductionPlanItems) + daily theo từng ngày
            String insertItemSql = """
            INSERT INTO ProductionPlanItems (PlanID, ProductID, PlannedQuantity, CreatedAt)
            OUTPUT INSERTED.PlanItemID
            VALUES (?, ?, ?, GETDATE())
        """;

            String insertDailySql = """
            INSERT INTO ProductionPlanDaily (PlanItemID, RunDate, Quantity, ActualQuantity)
            VALUES (?, ?, ?, 0)
        """;

            for (SelectedModelDto model : modelList) {
                Integer productId = jdbcTemplate.queryForObject(
                        "SELECT ProductID FROM Products WHERE ProductCode = ?", Integer.class, model.getModelCode());

                if (productId == null) {
                    System.err.println("Không tìm thấy ProductCode: " + model.getModelCode());
                    continue;
                }

                // Tạo item kế hoạch tuần
                Integer planItemId = jdbcTemplate.queryForObject(insertItemSql, Integer.class,
                        planId, productId, model.getQuantity());
                if (planItemId == null) continue;

                // Không gán Quantity ngay → khởi tạo daily rỗng
                for (int i = 0; i < 7; i++) {
                    LocalDate runDate = fromDate.plusDays(i);
                    jdbcTemplate.update(insertDailySql, planItemId, runDate, 0); // plan = 0, actual = 0
                }

            }

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


}
