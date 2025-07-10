package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlanDailyRowMapper implements RowMapper<ProductionPlanDaily> {
    @Override
    public ProductionPlanDaily mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductionPlanDaily daily = new ProductionPlanDaily();
        daily.setDailyID(rs.getInt("DailyID"));
        daily.setPlanItemID(rs.getInt("PlanItemID"));
        daily.setRunDate(rs.getObject("RunDate", LocalDate.class));
        daily.setQuantity(rs.getInt("Quantity"));
        daily.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        daily.setActualQuantity(rs.getInt("ActualQuantity"));
        return daily;
    }
}
