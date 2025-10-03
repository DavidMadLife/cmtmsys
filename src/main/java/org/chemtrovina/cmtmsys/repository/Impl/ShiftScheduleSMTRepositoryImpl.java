package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import org.chemtrovina.cmtmsys.repository.RowMapper.ShiftScheduleSMTRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ShiftScheduleSMTRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ShiftScheduleSMTRepositoryImpl implements ShiftScheduleSMTRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShiftScheduleSMTRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ShiftScheduleSMT shift) {
        String sql = """
        INSERT INTO ShiftScheduleSMT (WarehouseId, ShiftDate, ShiftType, StartTime, EndTime, CreatedAt)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, shift.getWarehouseId());
            ps.setDate(2, java.sql.Date.valueOf(shift.getShiftDate().toLocalDate()));
            ps.setString(3, shift.getShiftType());
            ps.setTimestamp(4, Timestamp.valueOf(shift.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(shift.getEndTime()));
            ps.setTimestamp(6, Timestamp.valueOf(
                    shift.getCreatedAt() != null ? shift.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        // 🔥 Gán lại ShiftId cho object vừa tạo
        Number key = keyHolder.getKey();
        if (key != null) {
            shift.setShiftId(key.intValue());
        }
    }


    @Override
    public void update(ShiftScheduleSMT shift) {
        String sql = "UPDATE ShiftScheduleSMT SET WarehouseId=?, ShiftDate=?, ShiftType=?, StartTime=?, EndTime=? WHERE ShiftId=?";
        jdbcTemplate.update(sql, shift.getWarehouseId(),
                shift.getShiftDate().toLocalDate(),
                shift.getShiftType(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getShiftId());
    }

    @Override
    public void deleteById(int shiftId) {
        String sql = "DELETE FROM ShiftScheduleSMT WHERE ShiftId=?";
        jdbcTemplate.update(sql, shiftId);
    }

    @Override
    public ShiftScheduleSMT findById(int shiftId) {
        String sql = "SELECT * FROM ShiftScheduleSMT WHERE ShiftId=?";
        List<ShiftScheduleSMT> list = jdbcTemplate.query(sql, new ShiftScheduleSMTRowMapper(), shiftId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ShiftScheduleSMT> findAll() {
        return jdbcTemplate.query("SELECT * FROM ShiftScheduleSMT ORDER BY ShiftDate, ShiftId", new ShiftScheduleSMTRowMapper());
    }

    @Override
    public List<ShiftScheduleSMT> findByDate(String date) {
        String sql = "SELECT * FROM ShiftScheduleSMT WHERE ShiftDate=?";
        return jdbcTemplate.query(sql, new ShiftScheduleSMTRowMapper(), date);
    }


    @Override
    public ShiftScheduleSMT findCurrentShift(int warehouseId, LocalDateTime time) {
        String sql = """
        SELECT TOP 1 *
        FROM ShiftScheduleSMT
        WHERE WarehouseId = ?
          AND StartTime <= ?
          AND EndTime >= ?
        ORDER BY StartTime DESC
    """;
        List<ShiftScheduleSMT> list = jdbcTemplate.query(
                sql,
                new ShiftScheduleSMTRowMapper(),
                warehouseId,
                Timestamp.valueOf(time),
                Timestamp.valueOf(time)
        );
        return list.isEmpty() ? null : list.get(0);
    }


}
