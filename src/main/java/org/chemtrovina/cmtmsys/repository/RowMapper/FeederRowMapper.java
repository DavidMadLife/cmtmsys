package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Feeder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeederRowMapper implements RowMapper<Feeder> {
    @Override
    public Feeder mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feeder feeder = new Feeder();
        feeder.setFeederId(rs.getInt("FeederID"));
        feeder.setModelLineId(rs.getInt("ModelLineID"));
        feeder.setFeederCode(rs.getString("FeederCode"));
        feeder.setSapCode(rs.getString("SapCode"));
        feeder.setQty(rs.getInt("Qty"));
        feeder.setMachine(rs.getString("Machine"));
        return feeder;
    }
}
