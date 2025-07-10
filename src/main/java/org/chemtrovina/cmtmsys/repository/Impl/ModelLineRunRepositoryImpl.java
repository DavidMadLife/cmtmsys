package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.chemtrovina.cmtmsys.repository.RowMapper.ModelLineRunRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRunRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModelLineRunRepositoryImpl implements ModelLineRunRepository {

    private final JdbcTemplate jdbcTemplate;

    public ModelLineRunRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ModelLineRun run) {
        String sql = "INSERT INTO ModelLineRuns (ModelLineID, StartedAt, Status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, run.getModelLineId(), run.getStartedAt(), run.getStatus());
    }

    @Override
    public void update(ModelLineRun run) {
        String sql = "UPDATE ModelLineRuns SET Status = ?, EndedAt = ? WHERE RunID = ?";
        jdbcTemplate.update(sql, run.getStatus(), run.getEndedAt(), run.getRunId());
    }

    @Override
    public void endRun(int runId) {
        String sql = "UPDATE ModelLineRuns SET Status = 'Completed', EndedAt = GETDATE() WHERE RunID = ?";
        jdbcTemplate.update(sql, runId);
    }

    @Override
    public ModelLineRun findById(int runId) {
        String sql = "SELECT * FROM ModelLineRuns WHERE RunID = ?";
        List<ModelLineRun> results = jdbcTemplate.query(sql, new ModelLineRunRowMapper(), runId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ModelLineRun> findByModelLineId(int modelLineId) {
        String sql = "SELECT * FROM ModelLineRuns WHERE ModelLineID = ? ORDER BY StartedAt DESC";
        return jdbcTemplate.query(sql, new ModelLineRunRowMapper(), modelLineId);
    }
}
