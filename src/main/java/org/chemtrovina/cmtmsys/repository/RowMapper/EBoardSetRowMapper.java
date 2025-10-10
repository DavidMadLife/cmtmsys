package org.chemtrovina.cmtmsys.repository.RowMapper;


import org.chemtrovina.cmtmsys.model.EBoardSet;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class EBoardSetRowMapper implements RowMapper<EBoardSet> {
    @Override
    public EBoardSet mapRow(ResultSet rs, int rowNum) throws SQLException {
        EBoardSet set = new EBoardSet();
        set.setSetId(rs.getInt("SetId"));
        set.setSetName(rs.getString("SetName"));
        set.setDescription(rs.getString("Description"));

        var created = rs.getTimestamp("CreatedAt");
        var updated = rs.getTimestamp("UpdatedAt");
        set.setCreatedAt(created != null ? created.toLocalDateTime() : LocalDateTime.now());
        set.setUpdatedAt(updated != null ? updated.toLocalDateTime() : LocalDateTime.now());

        return set;
    }
}
