package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.FeederRoll;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class FeederRollRowMapper implements RowMapper<FeederRoll> {
    @Override
    public FeederRoll mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeederRoll roll = new FeederRoll();
        roll.setFeederRollId(rs.getInt("FeederRollID"));
        roll.setFeederId(rs.getInt("FeederID"));
        roll.setMaterialId(rs.getInt("MaterialID"));
        roll.setRunId(rs.getInt("RunID"));

        Timestamp attachedAt = rs.getTimestamp("AttachedAt");
        if (attachedAt != null) {
            roll.setAttachedAt(attachedAt.toLocalDateTime());
        }

        Timestamp detachedAt = rs.getTimestamp("DetachedAt");
        if (detachedAt != null) {
            roll.setDetachedAt(detachedAt.toLocalDateTime());
        }

        roll.setActive(rs.getBoolean("IsActive"));
        return roll;
    }
}
