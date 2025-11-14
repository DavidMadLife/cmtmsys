package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
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
        product.setName(rs.getString("Name"));
        product.setDescription(rs.getString("Description"));

        // Set enum type (convert from string in DB)
        String typeStr = rs.getString("ModelType");
        if (typeStr != null) {
            try {
                product.setModelType(ModelType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                product.setModelType(ModelType.NONE); // fallback nếu không hợp lệ
            }
        }

        Timestamp created = rs.getTimestamp("CreatedDate");
        if (created != null) product.setCreatedDate(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("UpdatedDate");
        if (updated != null) product.setUpdatedDate(updated.toLocalDateTime());

        return product;
    }
}