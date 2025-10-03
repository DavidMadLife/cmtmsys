package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductionGapLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ProductionGapLogRowMapper implements RowMapper<ProductionGapLog> {
    @Override
    public ProductionGapLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductionGapLog gap = new ProductionGapLog();
        gap.setGapId(rs.getLong("GapId"));
        gap.setProductId(rs.getInt("ProductId"));
        gap.setWarehouseId(rs.getInt("WarehouseId"));
        gap.setPrevLogId(rs.getInt("PrevLogId"));
        gap.setCurrLogId(rs.getInt("CurrLogId"));
        gap.setPidDistanceSec(rs.getInt("PidDistanceSec"));
        gap.setStatus(rs.getString("Status"));
        gap.setReason(rs.getString("Reason"));

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) gap.setCreatedAt(created.toLocalDateTime());

        return gap;
    }
}
