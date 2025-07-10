package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RejectedMaterialRowMapper implements RowMapper<RejectedMaterial> {
    @Override
    public RejectedMaterial mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RejectedMaterial(
                rs.getInt("id"),
                rs.getInt("workOrderId"),
                rs.getInt("warehouseId"),
                rs.getString("sapCode"),
                rs.getInt("quantity"),
                rs.getTimestamp("createdDate").toLocalDateTime(),
                rs.getString("note")
        );
    }
}
