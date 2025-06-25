package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ProductBOMRowMapper implements RowMapper<ProductBOM> {
    @Override
    public ProductBOM mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductBOM bom = new ProductBOM();
        bom.setBomId(rs.getInt("BOMID"));
        bom.setProductId(rs.getInt("ProductID"));
        bom.setSappn(rs.getString("SAPPN"));
        bom.setQuantity(rs.getDouble("Quantity"));

        Timestamp created = rs.getTimestamp("CreatedDate");
        if (created != null) bom.setCreatedDate(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("UpdatedDate");
        if (updated != null) bom.setUpdatedDate(updated.toLocalDateTime());

        return bom;
    }
}
