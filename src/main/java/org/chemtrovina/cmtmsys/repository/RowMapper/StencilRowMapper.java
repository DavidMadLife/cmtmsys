package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Stencil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class StencilRowMapper implements RowMapper<Stencil> {
    @Override
    public Stencil mapRow(ResultSet rs, int rowNum) throws SQLException {
        Stencil stencil = new Stencil();

        stencil.setStencilId(rs.getInt("StencilId"));
        stencil.setBarcode(rs.getString("Barcode"));
        stencil.setStencilNo(rs.getString("StencilNo"));
        stencil.setProductId(rs.getInt("ProductId"));

        int whId = rs.getInt("CurrentWarehouseId");
        if (rs.wasNull()) {
            stencil.setCurrentWarehouseId(null);
        } else {
            stencil.setCurrentWarehouseId(whId);
        }

        stencil.setVersionLabel(rs.getString("VersionLabel"));
        stencil.setSize(rs.getString("Size"));
        stencil.setArrayCount(rs.getInt("ArrayCount"));
        stencil.setReceivedDate(rs.getDate("ReceivedDate").toLocalDate());
        stencil.setStatus(rs.getString("Status"));
        stencil.setNote(rs.getString("Note"));

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) {
            stencil.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("UpdatedAt");
        if (updated != null) {
            stencil.setUpdatedAt(updated.toLocalDateTime());
        }

        return stencil;
    }
}
