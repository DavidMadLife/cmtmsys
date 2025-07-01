package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.WorkOrderItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkOrderItemRowMapper implements RowMapper<WorkOrderItem> {
    @Override
    public WorkOrderItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WorkOrderItem(
                rs.getInt("ItemID"),
                rs.getInt("WorkOrderID"),
                rs.getInt("ProductID"),
                rs.getInt("Quantity"),
                rs.getTimestamp("CreatedDate").toLocalDateTime(),
                rs.getTimestamp("UpdatedDate").toLocalDateTime()
        );
    }
}
