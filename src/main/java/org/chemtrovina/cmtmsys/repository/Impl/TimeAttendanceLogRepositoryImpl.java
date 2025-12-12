package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.TimeAttendanceLog;
import org.chemtrovina.cmtmsys.repository.base.TimeAttendanceLogRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TimeAttendanceLogRepositoryImpl implements TimeAttendanceLogRepository {

    private final JdbcTemplate jdbc;
    private final BeanPropertyRowMapper<TimeAttendanceLog> mapper =
            new BeanPropertyRowMapper<>(TimeAttendanceLog.class);

    public TimeAttendanceLogRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ===================================
    // CRUD IMPLEMENTATION
    // ===================================

    @Override
    public List<TimeAttendanceLog> findAll() {
        String sql = """
            SELECT 
                LogId, 
                EmployeeId, 
                ScanDateTime, 
                ScanAction, 
                ScanMethod, 
                CreatedAt 
            FROM TimeAttendanceLog
            ORDER BY ScanDateTime DESC
        """;
        // BeanPropertyRowMapper được sử dụng
        return jdbc.query(sql, mapper);
    }

    @Override
    public TimeAttendanceLog findById(int id) {
        String sql = """
            SELECT 
                LogId, 
                EmployeeId, 
                ScanDateTime, 
                ScanAction, 
                ScanMethod, 
                CreatedAt 
            FROM TimeAttendanceLog
            WHERE LogId = ?
        """;

        List<TimeAttendanceLog> results = jdbc.query(sql, mapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public int insert(TimeAttendanceLog log) {
        String sql = """
            INSERT INTO TimeAttendanceLog(
                EmployeeId, 
                ScanDateTime, 
                ScanAction, 
                ScanMethod, 
                CreatedAt
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        // Lấy giá trị String từ Enum để lưu vào DB
        String scanActionStr = log.getScanAction().name();
        String scanMethodStr = log.getScanMethod().name();

        return jdbc.update(sql,
                log.getEmployeeId(),
                log.getScanDateTime(),
                scanActionStr, // Lưu Enum dưới dạng String (IN/OUT)
                scanMethodStr, // Lưu Enum dưới dạng String (FINGERPRINT/CARD)
                log.getCreatedAt());
    }

    @Override
    public int update(TimeAttendanceLog log) {
        String sql = """
            UPDATE TimeAttendanceLog
            SET 
                EmployeeId = ?, 
                ScanDateTime = ?, 
                ScanAction = ?, 
                ScanMethod = ?, 
                CreatedAt = ?
            WHERE LogId = ?
        """;

        // Lấy giá trị String từ Enum để lưu vào DB
        String scanActionStr = log.getScanAction().name();
        String scanMethodStr = log.getScanMethod().name();

        return jdbc.update(sql,
                log.getEmployeeId(),
                log.getScanDateTime(),
                scanActionStr, // Lưu Enum dưới dạng String (IN/OUT)
                scanMethodStr, // Lưu Enum dưới dạng String (FINGERPRINT/CARD)
                log.getCreatedAt(),
                log.getLogId());
    }

    @Override
    public int delete(int id) {
        String sql = "DELETE FROM TimeAttendanceLog WHERE LogId = ?";
        return jdbc.update(sql, id);
    }

    @Override
    public List<TimeAttendanceLog> findByScanDateRange(LocalDate from, LocalDate to) {
        String sql = """
            SELECT 
                LogId, 
                EmployeeId, 
                ScanDateTime, 
                ScanAction, 
                ScanMethod, 
                CreatedAt 
            FROM TimeAttendanceLog
            WHERE 
                ScanDateTime >= ? AND ScanDateTime < ?
            ORDER BY ScanDateTime DESC
        """;

        // Sử dụng to.plusDays(1) để bao gồm toàn bộ thời gian của ngày 'to'
        return jdbc.query(sql, mapper, from, to.plusDays(1));
    }

    public List<TimeAttendanceLog> findByEmployeeIdAndDate(int employeeId, LocalDate date) {
        String sql = """
            SELECT * FROM TimeAttendanceLog
            WHERE EmployeeId = ? 
            AND CAST(ScanDateTime AS DATE) = ?
            ORDER BY ScanDateTime DESC
        """;
        return jdbc.query(sql, mapper, employeeId, date);
    }
}