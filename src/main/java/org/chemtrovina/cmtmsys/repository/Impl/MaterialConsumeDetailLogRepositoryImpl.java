package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialConsumeDetailLogRowMapper;
import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeDetailLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MaterialConsumeDetailLogRepositoryImpl implements MaterialConsumeDetailLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialConsumeDetailLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(MaterialConsumeDetailLog log) {
        String sql = """
            INSERT INTO MaterialConsumeDetailLog (PlanItemID, RunDate, MaterialID, ConsumedQty, CreatedAt)
            VALUES (?, ?, ?, ?, GETDATE())
        """;
        jdbcTemplate.update(sql,
                log.getPlanItemId(),
                log.getRunDate(),
                log.getMaterialId(),
                log.getConsumedQty()
        );
    }

    @Override
    public List<MaterialConsumeDetailLog> findByPlanItemAndDate(int planItemId, LocalDate runDate) {
        String sql = "SELECT * FROM MaterialConsumeDetailLog WHERE PlanItemID = ? AND RunDate = ?";
        return jdbcTemplate.query(sql, new MaterialConsumeDetailLogRowMapper(), planItemId, runDate);
    }

    @Override
    public void deleteByPlanItemAndDate(int planItemId, LocalDate runDate) {
        String sql = "DELETE FROM MaterialConsumeDetailLog WHERE PlanItemID = ? AND RunDate = ?";
        jdbcTemplate.update(sql, planItemId, runDate);
    }
}
