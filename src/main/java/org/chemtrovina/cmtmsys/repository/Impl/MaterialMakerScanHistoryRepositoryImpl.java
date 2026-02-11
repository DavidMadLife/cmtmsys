package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.MaterialMakerScanHistory;
import org.chemtrovina.cmtmsys.model.enums.ScanResult;
import org.chemtrovina.cmtmsys.repository.base.MaterialMakerScanHistoryRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class MaterialMakerScanHistoryRepositoryImpl implements MaterialMakerScanHistoryRepository {

    private final JdbcTemplate jdbc;
    private final BeanPropertyRowMapper<MaterialMakerScanHistory> mapper =
            new BeanPropertyRowMapper<>(MaterialMakerScanHistory.class);

    public MaterialMakerScanHistoryRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<MaterialMakerScanHistory> findAll() {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            ORDER BY ScanAt DESC, Id DESC
        """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public MaterialMakerScanHistory findById(int id) {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            WHERE Id = ?
        """;
        List<MaterialMakerScanHistory> list = jdbc.query(sql, mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int insert(MaterialMakerScanHistory h) {
        String sql = """
            INSERT INTO MaterialMakerScanHistory(
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        return jdbc.update(sql,
                h.getRollCode(),
                h.getMakerPN(),
                h.getExpectedMakerPN(),
                h.getSpec(),
                h.getMaker(),
                h.getResult() == null ? null : h.getResult().name(),
                h.getMessage(),
                h.getEmployeeId(),
                h.getScanAt()
        );
    }

    @Override
    public int delete(int id) {
        String sql = "DELETE FROM MaterialMakerScanHistory WHERE Id = ?";
        return jdbc.update(sql, id);
    }

    @Override
    public List<MaterialMakerScanHistory> findByScanDateRange(LocalDate from, LocalDate toExclusive) {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            WHERE ScanAt >= ? AND ScanAt < ?
            ORDER BY ScanAt DESC, Id DESC
        """;

        return jdbc.query(sql, mapper,
                java.sql.Timestamp.valueOf(from.atStartOfDay()),
                java.sql.Timestamp.valueOf(toExclusive.atStartOfDay())
        );
    }

    @Override
    public List<MaterialMakerScanHistory> findByRollCode(String rollCode) {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            WHERE RollCode = ?
            ORDER BY ScanAt DESC, Id DESC
        """;
        return jdbc.query(sql, mapper, rollCode);
    }

    @Override
    public List<MaterialMakerScanHistory> findByEmployeeIdAndDate(String employeeId, LocalDate date) {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            WHERE EmployeeId = ?
              AND CAST(ScanAt AS DATE) = ?
            ORDER BY ScanAt DESC, Id DESC
        """;
        return jdbc.query(sql, mapper, employeeId, date);
    }

    @Override
    public List<MaterialMakerScanHistory> findByResultAndDate(ScanResult result, LocalDate date) {
        String sql = """
            SELECT
                Id,
                RollCode,
                MakerPN,
                ExpectedMakerPN,
                Spec,
                Maker,
                Result,
                Message,
                EmployeeId,
                ScanAt
            FROM MaterialMakerScanHistory
            WHERE Result = ?
              AND CAST(ScanAt AS DATE) = ?
            ORDER BY ScanAt DESC, Id DESC
        """;
        return jdbc.query(sql, mapper, result.name(), date);
    }
}
