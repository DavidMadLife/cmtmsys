package org.chemtrovina.cmtmsys.repository.RowMapper;


import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class ProductionPlanHourlyRowMapper implements RowMapper<ProductionPlanHourly> {
    @Override
    public ProductionPlanHourly mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductionPlanHourly plan = new ProductionPlanHourly();
        plan.setHourlyId(rs.getInt("HourlyID"));
        plan.setDailyId(rs.getInt("DailyID"));
        plan.setSlotIndex(rs.getInt("SlotIndex"));
        plan.setRunHour(rs.getTimestamp("RunHour").toLocalDateTime());
        plan.setPlanQuantity(rs.getInt("PlanQuantity"));
        plan.setActualQuantity(rs.getInt("ActualQuantity"));
        plan.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        plan.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
        return plan;
    }
}