package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.FeederAssignment;
import org.chemtrovina.cmtmsys.repository.RowMapper.FeederAssignmentRowMapper;
import org.chemtrovina.cmtmsys.repository.base.FeederAssignmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FeederAssignmentRepositoryImpl implements FeederAssignmentRepository {
    private final JdbcTemplate jdbc;

    public FeederAssignmentRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(FeederAssignment assignment) {
        String sql = "INSERT INTO FeederAssignments (RunID, FeederID, AssignedAt, AssignedBy) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, assignment.getRunId(), assignment.getFeederId(), assignment.getAssignedAt(), assignment.getAssignedBy());
    }

    @Override
    public List<FeederAssignment> findByRunId(int runId) {
        String sql = "SELECT * FROM FeederAssignments WHERE RunID = ?";
        return jdbc.query(sql, new FeederAssignmentRowMapper(), runId);
    }

    @Override
    public FeederAssignment findByRunAndFeeder(int runId, int feederId) {
        String sql = "SELECT * FROM FeederAssignments WHERE RunID = ? AND FeederID = ?";
        List<FeederAssignment> list = jdbc.query(sql, new FeederAssignmentRowMapper(), runId, feederId);
        return list.isEmpty() ? null : list.get(0);
    }
}
