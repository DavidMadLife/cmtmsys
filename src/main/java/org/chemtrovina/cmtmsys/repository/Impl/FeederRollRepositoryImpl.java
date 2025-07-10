package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.FeederRoll;
import org.chemtrovina.cmtmsys.repository.RowMapper.FeederRollRowMapper;
import org.chemtrovina.cmtmsys.repository.base.FeederRollRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FeederRollRepositoryImpl implements FeederRollRepository {

    private final JdbcTemplate jdbcTemplate;

    public FeederRollRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void attachRoll(FeederRoll roll) {
        String sql = """
            INSERT INTO FeederRolls (FeederID, MaterialID, RunID, AttachedAt, IsActive)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, roll.getFeederId(), roll.getMaterialId(), roll.getRunId(), roll.getAttachedAt(), roll.isActive());
    }

    @Override
    public void detachRoll(int feederId, int runId) {
        String sql = """
            UPDATE FeederRolls
            SET DetachedAt = GETDATE(), IsActive = 0
            WHERE FeederID = ? AND RunID = ? AND IsActive = 1
        """;
        jdbcTemplate.update(sql, feederId, runId);
    }

    @Override
    public List<FeederRoll> findActiveByRun(int runId) {
        String sql = "SELECT * FROM FeederRolls WHERE RunID = ? AND IsActive = 1";
        return jdbcTemplate.query(sql, new FeederRollRowMapper(), runId);
    }

    @Override
    public List<FeederRoll> findAllByFeeder(int feederId) {
        String sql = "SELECT * FROM FeederRolls WHERE FeederID = ? ORDER BY AttachedAt DESC";
        return jdbcTemplate.query(sql, new FeederRollRowMapper(), feederId);
    }
}
