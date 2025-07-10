package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WarehouseTransferDetailRowMapper implements RowMapper<WarehouseTransferDetail> {
    @Override
    public WarehouseTransferDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        WarehouseTransferDetail d = new WarehouseTransferDetail();
        d.setTransferDetailId(rs.getInt("TransferDetailID"));
        d.setTransferId(rs.getInt("TransferID"));
        d.setRollCode(rs.getString("RollCode"));
        d.setSapCode(rs.getString("SAPCode"));
        d.setQuantity(rs.getInt("Quantity"));
        // ✅ Cập nhật thêm 2 cột mới
        d.setActualReturned(rs.getInt("ActualReturned")); // hoặc rs.getInt("actualReturned") tùy theo tên cột DB
        d.setActive(rs.getBoolean("Active"));
        d.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
        return d;
    }
}
