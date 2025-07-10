package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


@Repository
public class MaterialConsumeLogRepositoryImpl implements MaterialConsumeLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialConsumeLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(int planItemId, LocalDate runDate) {
        String sql = "SELECT COUNT(*) FROM MaterialConsumeLog WHERE PlanItemID = ? AND RunDate = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, planItemId, runDate);
        return count != null && count > 0;
    }

    @Override
    public void insert(int planItemId, LocalDate runDate, int consumedQty) {
        String sql = "INSERT INTO MaterialConsumeLog (PlanItemID, RunDate, ConsumedQty) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, planItemId, runDate, consumedQty);
    }

    @Override
    public void delete(int planItemId, LocalDate runDate) {
        String sql = "DELETE FROM MaterialConsumeLog WHERE PlanItemID = ? AND RunDate = ?";
        jdbcTemplate.update(sql, planItemId, runDate);
    }

    @Override
    public int getConsumedQty(int planItemId, LocalDate runDate) {
        String sql = "SELECT COALESCE(ConsumedQty, 0) FROM MaterialConsumeLog WHERE PlanItemID = ? AND RunDate = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, planItemId, runDate);
    }

}
