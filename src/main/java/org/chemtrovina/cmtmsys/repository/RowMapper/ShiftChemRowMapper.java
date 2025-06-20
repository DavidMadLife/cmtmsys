package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ShiftChem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShiftChemRowMapper implements RowMapper<ShiftChem> {
    @Override
    public ShiftChem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ShiftChem(
                rs.getInt("ShiftId"),
                rs.getString("ShiftName")
        );
    }
}