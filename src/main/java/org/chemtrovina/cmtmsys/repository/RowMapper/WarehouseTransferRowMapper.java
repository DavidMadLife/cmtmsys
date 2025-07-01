package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WarehouseTransferRowMapper implements RowMapper<WarehouseTransfer> {
    @Override
    public WarehouseTransfer mapRow(ResultSet rs, int rowNum) throws SQLException {
        WarehouseTransfer wt = new WarehouseTransfer();
        wt.setTransferId(rs.getInt("TransferID"));
        wt.setWorkOrderId(rs.getInt("WorkOrderID"));
        wt.setFromWarehouseId(rs.getInt("FromWarehouseID"));
        wt.setToWarehouseId(rs.getInt("ToWarehouseID"));
        wt.setTransferDate(rs.getTimestamp("TransferDate").toLocalDateTime());
        wt.setNote(rs.getString("Note"));
        wt.setEmployeeId(rs.getString("EmployeeID"));
        return wt;
    }
}
