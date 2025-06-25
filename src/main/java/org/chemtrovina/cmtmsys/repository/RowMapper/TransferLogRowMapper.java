package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.TransferLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferLogRowMapper implements RowMapper<TransferLog> {
    @Override
    public TransferLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TransferLog(
                rs.getInt("TransferID"),
                rs.getString("RollCode"),
                rs.getInt("FromWarehouseID"),
                rs.getInt("ToWarehouseID"),
                rs.getTimestamp("TransferDate").toLocalDateTime(),
                rs.getString("Note"),
                rs.getString("EmployeeID")
        );
    }
}
