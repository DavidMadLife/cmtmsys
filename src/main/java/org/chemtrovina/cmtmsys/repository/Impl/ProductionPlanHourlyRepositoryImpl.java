package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductionPlanHourlyRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanHourlyRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductionPlanHourlyRepositoryImpl implements ProductionPlanHourlyRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductionPlanHourlyRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(ProductionPlanHourly hourly) {
        String sql = """
            INSERT INTO ProductionPlanHourly (DailyID, SlotIndex, RunHour, PlanQuantity, ActualQuantity, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;
        jdbcTemplate.update(sql,
                hourly.getDailyId(),
                hourly.getSlotIndex(),
                Timestamp.valueOf(hourly.getRunHour()),
                hourly.getPlanQuantity(),
                hourly.getActualQuantity()
        );
    }

    @Override
    public void updatePlanQuantity(int id, int quantity) {
        String sql = "UPDATE ProductionPlanHourly SET PlanQuantity = ?, UpdatedAt = GETDATE() WHERE HourlyID = ?";
        jdbcTemplate.update(sql, quantity, id);
    }

    @Override
    public void updateActualQuantity(int id, int quantity) {
        String sql = "UPDATE ProductionPlanHourly SET ActualQuantity = ?, UpdatedAt = GETDATE() WHERE HourlyID = ?";
        jdbcTemplate.update(sql, quantity, id);
    }

    @Override
    public List<ProductionPlanHourly> findByDailyId(int dailyId) {
        String sql = "SELECT * FROM ProductionPlanHourly WHERE DailyID = ? ORDER BY SlotIndex";
        return jdbcTemplate.query(sql, new ProductionPlanHourlyRowMapper(), dailyId);
    }

    @Override
    public void deleteByDailyId(int dailyId) {
        String sql = "DELETE FROM ProductionPlanHourly WHERE DailyID = ?";
        jdbcTemplate.update(sql, dailyId);
    }

    @Override
    public void saveOrUpdate(ProductionPlanHourly plan) {
        String sql = """
            MERGE ProductionPlanHourly AS target
            USING (SELECT ? AS DailyID, ? AS SlotIndex) AS source
            ON target.DailyID = source.DailyID AND target.SlotIndex = source.SlotIndex
            WHEN MATCHED THEN
                UPDATE SET PlanQuantity = ?, ActualQuantity = ?, UpdatedAt = GETDATE()
            WHEN NOT MATCHED THEN
                INSERT (DailyID, SlotIndex, RunHour, PlanQuantity, ActualQuantity, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, GETDATE(), GETDATE());
        """;

        jdbcTemplate.update(sql,
                plan.getDailyId(), plan.getSlotIndex(),
                plan.getPlanQuantity(), plan.getActualQuantity(),
                plan.getDailyId(), plan.getSlotIndex(), Timestamp.valueOf(plan.getRunHour()),
                plan.getPlanQuantity(), plan.getActualQuantity()
        );
    }

    @Override
    public void saveOrUpdateBulk(List<ProductionPlanHourly> plans) {
        for (ProductionPlanHourly plan : plans) {
            saveOrUpdate(plan);
        }
    }

    @Override
    public List<ProductionPlanHourly> findByDateAndLine(LocalDate date, String line) {
        String sql = """
            SELECT h.*
            FROM ProductionPlanHourly h
            JOIN ProductionPlanDaily d ON h.DailyID = d.DailyID
            WHERE CAST(h.RunHour AS DATE) = ? AND d.Line = ?
            ORDER BY h.DailyID, h.SlotIndex
        """;
        return jdbcTemplate.query(sql, new ProductionPlanHourlyRowMapper(), date, line);
    }
}
