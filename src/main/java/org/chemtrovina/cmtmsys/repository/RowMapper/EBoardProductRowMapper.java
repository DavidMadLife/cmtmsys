package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.EBoardProduct;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EBoardProductRowMapper implements RowMapper<EBoardProduct> {
    @Override
    public EBoardProduct mapRow(ResultSet rs, int rowNum) throws SQLException {
        EBoardProduct ep = new EBoardProduct();
        ep.setId(rs.getInt("Id"));
        ep.setSetId(rs.getInt("SetId"));
        ep.setProductId(rs.getInt("ProductId"));
        ep.setCircuitType(rs.getString("CircuitType"));
        ep.setDescription(rs.getString("Description"));
        return ep;
    }
}

