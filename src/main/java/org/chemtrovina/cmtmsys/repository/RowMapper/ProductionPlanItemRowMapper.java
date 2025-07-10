package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductionPlanItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProductionPlanItemRowMapper implements RowMapper<ProductionPlanItem> {
    @Override
    public ProductionPlanItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductionPlanItem item = new ProductionPlanItem();
        item.setPlanItemID(rs.getInt("PlanItemID"));
        item.setPlanID(rs.getInt("PlanID"));
        item.setProductID(rs.getInt("ProductID"));
        item.setPlannedQuantity(rs.getInt("PlannedQuantity"));
        item.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return item;
    }
}
