package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Warehouse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WarehouseRowMapper implements RowMapper<Warehouse> {
    @Override
    public Warehouse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Warehouse(
                rs.getInt("WarehouseID"),
                rs.getString("Name")
        );
    }
}
