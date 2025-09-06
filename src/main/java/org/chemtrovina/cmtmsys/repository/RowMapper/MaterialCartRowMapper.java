package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.MaterialCart;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MaterialCartRowMapper implements RowMapper<MaterialCart> {
    @Override
    public MaterialCart mapRow(ResultSet rs, int rowNum) throws SQLException {
        MaterialCart cart = new MaterialCart();
        cart.setCartId(rs.getInt("CartID"));
        cart.setCartCode(rs.getString("CartCode"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        cart.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return cart;
    }
}
