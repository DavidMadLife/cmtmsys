package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.chemtrovina.cmtmsys.repository.RowMapper.ModelLineRunRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRunRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ModelLineRunRepositoryImpl implements ModelLineRunRepository {
    private final JdbcTemplate jdbc;

    public ModelLineRunRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(ModelLineRun run) {
        String sql = "INSERT INTO ModelLineRuns (ModelLineID, StartedAt, Status) VALUES (?, ?, ?)";
        jdbc.update(sql, run.getModelLineId(), run.getStartedAt(), run.getStatus());
    }

    @Override
    public void update(ModelLineRun run) {
        String sql = "UPDATE ModelLineRuns SET Status = ?, EndedAt = ? WHERE RunID = ?";
        jdbc.update(sql, run.getStatus(), run.getEndedAt(), run.getRunId());
    }

    @Override
    public ModelLineRun findById(int runId) {
        String sql = "SELECT * FROM ModelLineRuns WHERE RunID = ?";
        List<ModelLineRun> list = jdbc.query(sql, new ModelLineRunRowMapper(), runId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ModelLineRun> findByModelLineId(int modelLineId) {
        String sql = "SELECT * FROM ModelLineRuns WHERE ModelLineID = ? ORDER BY StartedAt DESC";
        return jdbc.query(sql, new ModelLineRunRowMapper(), modelLineId);
    }

    @Override
    public void markRunsAsDuplicate(int modelLineId, LocalDate date) {
        String sql = """
        UPDATE ModelLineRuns 
        SET Status = 'Duplicate'
        WHERE ModelLineID = ? 
          AND CAST(StartedAt AS DATE) = ?
          AND Status = 'Running'
    """;
        jdbc.update(sql, modelLineId, date);
    }

    @Override
    public ModelLineRun findLatestRunByModelLineId(int modelLineId) {
        String sql = """
        SELECT TOP 1 * 
        FROM ModelLineRuns 
        WHERE ModelLineID = ? 
        ORDER BY StartedAt DESC
    """;
        return jdbc.queryForObject(sql, new ModelLineRunRowMapper(), modelLineId);
    }


}
