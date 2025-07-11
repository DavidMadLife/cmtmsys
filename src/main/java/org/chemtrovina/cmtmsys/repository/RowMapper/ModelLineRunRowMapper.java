package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ModelLineRunRowMapper implements RowMapper<ModelLineRun> {
    @Override
    public ModelLineRun mapRow(ResultSet rs, int rowNum) throws SQLException {
        ModelLineRun run = new ModelLineRun();
        run.setRunId(rs.getInt("RunID"));
        run.setModelLineId(rs.getInt("ModelLineID"));

        Timestamp startedAt = rs.getTimestamp("StartedAt");
        if (startedAt != null) run.setStartedAt(startedAt.toLocalDateTime());

        Timestamp endedAt = rs.getTimestamp("EndedAt");
        if (endedAt != null) run.setEndedAt(endedAt.toLocalDateTime());

        run.setStatus(rs.getString("Status"));
        return run;
    }
}
