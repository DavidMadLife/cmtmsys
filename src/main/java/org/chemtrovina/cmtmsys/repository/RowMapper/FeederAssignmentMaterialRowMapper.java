package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.FeederAssignmentMaterial;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FeederAssignmentMaterialRowMapper implements RowMapper<FeederAssignmentMaterial> {
    @Override
    public FeederAssignmentMaterial mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeederAssignmentMaterial material = new FeederAssignmentMaterial();
        material.setId(rs.getInt("Id"));
        material.setAssignmentId(rs.getInt("AssignmentID"));
        material.setMaterialId(rs.getInt("MaterialID"));
        material.setSupplement(rs.getBoolean("IsSupplement"));
        material.setActive(rs.getBoolean("IsActive"));
        material.setNote(rs.getString("Note"));

        Timestamp attachedAt = rs.getTimestamp("AttachedAt");
        if (attachedAt != null) material.setAttachedAt(attachedAt.toLocalDateTime());

        Timestamp detachedAt = rs.getTimestamp("DetachedAt");
        if (detachedAt != null) material.setDetachedAt(detachedAt.toLocalDateTime());

        return material;
    }
}
