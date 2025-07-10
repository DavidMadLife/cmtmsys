package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ModelLine;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelLineRowMapper implements RowMapper<ModelLine> {
    @Override
    public ModelLine mapRow(ResultSet rs, int rowNum) throws SQLException {
        ModelLine modelLine = new ModelLine();
        modelLine.setModelLineId(rs.getInt("ModelLineID"));
        modelLine.setProductId(rs.getInt("ProductID"));
        modelLine.setWarehouseId(rs.getInt("WarehouseID"));
        return modelLine;
    }
}
