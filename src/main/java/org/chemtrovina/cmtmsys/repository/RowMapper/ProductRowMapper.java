package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ProductRowMapper implements RowMapper<Product> {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("ProductID"));
        product.setProductCode(rs.getString("ProductCode"));
        product.setDescription(rs.getString("Description"));

        Timestamp created = rs.getTimestamp("CreatedDate");
        if (created != null) product.setCreatedDate(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("UpdatedDate");
        if (updated != null) product.setUpdatedDate(updated.toLocalDateTime());

        return product;
    }
}
