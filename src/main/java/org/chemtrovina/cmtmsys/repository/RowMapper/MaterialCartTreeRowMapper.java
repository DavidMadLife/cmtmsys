package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.MaterialCartTree;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MaterialCartTreeRowMapper implements RowMapper<MaterialCartTree> {
    @Override
    public MaterialCartTree mapRow(ResultSet rs, int rowNum) throws SQLException {
        MaterialCartTree tree = new MaterialCartTree();
        tree.setTreeId(rs.getInt("TreeID"));
        tree.setCartId(rs.getInt("CartID"));
        tree.setTreeCode(rs.getString("TreeCode"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        tree.setLevelNote(rs.getString("LevelNote"));
        tree.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return tree;
    }
}
