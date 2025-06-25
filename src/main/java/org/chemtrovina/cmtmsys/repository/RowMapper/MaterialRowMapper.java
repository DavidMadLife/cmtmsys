package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Material;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MaterialRowMapper implements RowMapper<Material> {
    @Override
    public Material mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Material(
                rs.getInt("MaterialID"),
                rs.getString("SapCode"),
                rs.getString("RollCode"),
                rs.getInt("Quantity"),
                rs.getInt("WarehouseID"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
                rs.getString("Spec"),
                rs.getString("EmployeeID")
        );
    }
}
