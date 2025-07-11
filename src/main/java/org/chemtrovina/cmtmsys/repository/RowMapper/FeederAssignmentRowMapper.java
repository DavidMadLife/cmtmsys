package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.FeederAssignment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FeederAssignmentRowMapper implements RowMapper<FeederAssignment> {
    @Override
    public FeederAssignment mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeederAssignment assignment = new FeederAssignment();
        assignment.setAssignmentId(rs.getInt("AssignmentID"));
        assignment.setRunId(rs.getInt("RunID"));
        assignment.setFeederId(rs.getInt("FeederID"));

        Timestamp assignedAt = rs.getTimestamp("AssignedAt");
        if (assignedAt != null) assignment.setAssignedAt(assignedAt.toLocalDateTime());

        assignment.setAssignedBy(rs.getString("AssignedBy"));
        return assignment;
    }
}
