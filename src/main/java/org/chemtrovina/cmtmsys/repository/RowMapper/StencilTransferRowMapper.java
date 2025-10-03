package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.StencilTransfer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class StencilTransferRowMapper implements RowMapper<StencilTransfer> {
    @Override
    public StencilTransfer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new StencilTransfer(
                rs.getInt("TransferId"),
                rs.getInt("StencilId"),
                rs.getString("Barcode"),
                rs.getObject("FromWarehouseId") != null ? rs.getInt("FromWarehouseId") : null,
                rs.getObject("ToWarehouseId") != null ? rs.getInt("ToWarehouseId") : null,
                rs.getTimestamp("TransferDate").toLocalDateTime(),
                rs.getString("PerformedBy"),
                rs.getString("Note")
        );
    }
}
