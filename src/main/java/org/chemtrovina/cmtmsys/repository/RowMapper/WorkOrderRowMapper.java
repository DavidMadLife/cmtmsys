package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class WorkOrderRowMapper implements RowMapper<WorkOrder> {
    @Override
    public WorkOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WorkOrder(
                rs.getInt("WorkOrderID"),
                rs.getString("WorkOrderCode"),
                rs.getString("Description"),
                rs.getTimestamp("CreatedDate").toLocalDateTime(),
                rs.getTimestamp("UpdatedDate").toLocalDateTime()
        );
    }
}
