package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ProductCycleTime;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProductCycleTimeRowMapper implements RowMapper<ProductCycleTime> {

    @Override
    public ProductCycleTime mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductCycleTime pct = new ProductCycleTime();
        pct.setCtId(rs.getInt("CtId"));
        pct.setProductId(rs.getInt("ProductId"));
        pct.setWarehouseId(rs.getInt("WarehouseId"));
        pct.setCtSeconds(rs.getBigDecimal("CtSeconds")); // giây
        pct.setArray(rs.getInt("Array"));                // << thêm dòng này
        pct.setActive(rs.getBoolean("Active"));
        pct.setVersion(rs.getObject("Version") != null ? rs.getInt("Version") : null);
        pct.setNote(rs.getString("Note"));
        pct.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return pct;
    }
}
