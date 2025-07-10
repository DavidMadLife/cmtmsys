package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductionPlan;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlanRowMapper implements RowMapper<ProductionPlan> {
    @Override
    public ProductionPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductionPlan plan = new ProductionPlan();
        plan.setPlanID(rs.getInt("PlanID"));
        plan.setLineWarehouseID(rs.getInt("LineWarehouseID"));
        plan.setWeekNo(rs.getInt("WeekNo"));
        plan.setYear(rs.getInt("Year"));
        plan.setFromDate(rs.getObject("FromDate", LocalDate.class));
        plan.setToDate(rs.getObject("ToDate", LocalDate.class));
        plan.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return plan;
    }
}
