package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ShiftSchedule;
import org.chemtrovina.cmtmsys.repository.RowMapper.ShiftScheduleRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ShiftScheduleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.time.LocalDate;

@Repository
public class ShiftScheduleRepositoryImpl implements ShiftScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShiftScheduleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ShiftSchedule s) {
        String sql = "INSERT INTO ShiftSchedule (EmployeeID, WorkDate, ShiftID, Note, CreatedAt, UpdatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                s.getEmployeeId(),
                s.getWorkDate(),
                s.getShiftId(),
                s.getNote(),
                Timestamp.valueOf(s.getCreatedAt()),
                Timestamp.valueOf(s.getUpdatedAt())
        );
    }

    @Override
    public void update(ShiftSchedule s) {
        String sql = "UPDATE ShiftSchedule SET ShiftID = ?, Note = ?, UpdatedAt = ? WHERE ID = ?";
        jdbcTemplate.update(sql,
                s.getShiftId(),
                s.getNote(),
                Timestamp.valueOf(s.getUpdatedAt()),
                s.getId()
        );
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM ShiftSchedule WHERE ID = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public ShiftSchedule findById(int id) {
        String sql = "SELECT * FROM ShiftSchedule WHERE ID = ?";
        List<ShiftSchedule> results = jdbcTemplate.query(sql, new ShiftScheduleRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ShiftSchedule> findAll() {
        String sql = "SELECT * FROM ShiftSchedule";
        return jdbcTemplate.query(sql, new ShiftScheduleRowMapper());
    }

    @Override
    public List<ShiftSchedule> findByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to) {
        String sql = "SELECT * FROM ShiftSchedule WHERE EmployeeID = ? AND WorkDate BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, new ShiftScheduleRowMapper(), employeeId, from, to);
    }
}
