package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;
import org.chemtrovina.cmtmsys.repository.base.EBoardPerformanceLogRepository;
import org.chemtrovina.cmtmsys.repository.RowMapper.EBoardPerformanceLogRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EBoardPerformanceLogRepositoryImpl implements EBoardPerformanceLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public EBoardPerformanceLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(EBoardPerformanceLog log) {
        String sql = """
            INSERT INTO EBoardPerformanceLogs
            (EBoardProductId, SetId, WarehouseId, CircuitType, ModelType,
             CarrierId, AoiMachineCode, TotalModules, NgModules, Performance, LogFileName, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;
        jdbcTemplate.update(sql,
                log.geteBoardProductId(),
                log.getSetId(),
                log.getWarehouseId(),
                log.getCircuitType(),
                log.getModelType(),
                log.getCarrierId(),
                log.getAoiMachineCode(),
                log.getTotalModules(),
                log.getNgModules(),
                log.getPerformance(),
                log.getLogFileName());
    }

    @Override
    public List<EBoardPerformanceLog> findAll() {
        String sql = "SELECT * FROM EBoardPerformanceLogs ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new EBoardPerformanceLogRowMapper());
    }

    @Override
    public List<EBoardPerformanceLog> findBySet(int setId) {
        String sql = "SELECT * FROM EBoardPerformanceLogs WHERE SetId = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new EBoardPerformanceLogRowMapper(), setId);
    }

    @Override
    public List<EBoardPerformanceLog> findBySetAndCircuit(int setId, String circuitType) {
        String sql = "SELECT * FROM EBoardPerformanceLogs WHERE SetId = ? AND CircuitType = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new EBoardPerformanceLogRowMapper(), setId, circuitType);
    }

    @Override
    public EBoardPerformanceLog findLatestBySetAndCircuit(int setId, String circuitType) {
        String sql = """
            SELECT TOP 1 * FROM EBoardPerformanceLogs
            WHERE SetId = ? AND CircuitType = ?
            ORDER BY CreatedAt DESC
        """;
        return jdbcTemplate.query(sql, new EBoardPerformanceLogRowMapper(), setId, circuitType)
                .stream().findFirst().orElse(null);
    }


}
