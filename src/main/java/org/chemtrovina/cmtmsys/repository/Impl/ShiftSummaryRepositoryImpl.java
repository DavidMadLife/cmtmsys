package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.model.ShiftSummary;
import org.chemtrovina.cmtmsys.repository.RowMapper.ShiftSummaryRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ShiftSummaryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ShiftSummaryRepositoryImpl implements ShiftSummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShiftSummaryRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ShiftSummary summary) {
        String sql = """
    INSERT INTO ShiftSummary (
        ShiftId, WarehouseId, TotalTimeSec,
        TorTimeSec, TorQty, TorPercent,
        PorTimeSec, PorQty, PorPercent,
        IdleTimeSec, IdleQty, IdlePercent,
        McTimeSec, McQty, McPercent
    )
    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
""";

        jdbcTemplate.update(sql,
                summary.getShiftId(),
                summary.getWarehouseId(),
                summary.getTotalTimeSec(),

                summary.getTorTimeSec(),
                summary.getTorQty(),
                summary.getTorPercent(),

                summary.getPorTimeSec(),
                summary.getPorQty(),
                summary.getPorPercent(),

                summary.getIdleTimeSec(),
                summary.getIdleQty(),
                summary.getIdlePercent(),

                summary.getMcTimeSec(),
                summary.getMcQty(),
                summary.getMcPercent()
        );

    }

    @Override
    public void update(ShiftSummary summary) {
        String sql = """
    UPDATE ShiftSummary
    SET WarehouseId=?, TotalTimeSec=?,
        TorTimeSec=?, TorQty=?, TorPercent=?,
        PorTimeSec=?, PorQty=?, PorPercent=?,
        IdleTimeSec=?, IdleQty=?, IdlePercent=?,
        McTimeSec=?, McQty=?, McPercent=?
    WHERE SummaryId=?
""";

        jdbcTemplate.update(sql,
                summary.getWarehouseId(),
                summary.getTotalTimeSec(),

                summary.getTorTimeSec(),
                summary.getTorQty(),
                summary.getTorPercent(),

                summary.getPorTimeSec(),
                summary.getPorQty(),
                summary.getPorPercent(),

                summary.getIdleTimeSec(),
                summary.getIdleQty(),
                summary.getIdlePercent(),

                summary.getMcTimeSec(),
                summary.getMcQty(),
                summary.getMcPercent(),

                summary.getSummaryId()
        );

    }

    @Override
    public void deleteById(long summaryId) {
        jdbcTemplate.update("DELETE FROM ShiftSummary WHERE SummaryId=?", summaryId);
    }

    @Override
    public ShiftSummary findById(long summaryId) {
        String sql = "SELECT * FROM ShiftSummary WHERE SummaryId=?";
        List<ShiftSummary> list = jdbcTemplate.query(sql, new ShiftSummaryRowMapper(), summaryId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ShiftSummary> findAll() {
        return jdbcTemplate.query("SELECT * FROM ShiftSummary ORDER BY CreatedAt DESC", new ShiftSummaryRowMapper());
    }

    @Override
    public List<ShiftSummary> findByShift(int shiftId) {
        String sql = "SELECT * FROM ShiftSummary WHERE ShiftId=? ORDER BY WarehouseId";
        return jdbcTemplate.query(sql, new ShiftSummaryRowMapper(), shiftId);
    }

    private ShiftSummaryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ShiftSummaryDTO dto = new ShiftSummaryDTO();
        dto.setWarehouseName(rs.getString("WarehouseName"));
        dto.setStartTime(rs.getString("StartTime"));
        dto.setEndTime(rs.getString("EndTime"));

        dto.setPorTimeSec(rs.getInt("PorTimeSec"));
        dto.setPorQty(rs.getInt("PorQty"));
        dto.setPorPercent(rs.getDouble("PorPercent"));

        dto.setTorTimeSec(rs.getInt("TorTimeSec"));
        dto.setTorQty(rs.getInt("TorQty"));
        dto.setTorPercent(rs.getDouble("TorPercent"));

        dto.setIdleStart(rs.getString("IdleStart"));
        dto.setIdleQty(rs.getInt("IdleQty"));
        dto.setIdleTimeSec(rs.getInt("IdleTimeSec"));

        dto.setMcStart(rs.getString("McStart"));
        dto.setMcQty(rs.getInt("McQty"));
        return dto;
    }

    @Override
    public List<ShiftSummaryDTO> findByDateAndShiftType(LocalDate date, String shiftType) {
        String sql = """
            SELECT w.Name AS WarehouseName,
                   s.StartTime, s.EndTime,
                   sum.TorTimeSec, sum.TorQty, sum.TorPercent,
                   sum.PorTimeSec, sum.PorQty, sum.PorPercent,
                   sum.IdleTimeSec, sum.IdleQty, sum.IdlePercent,
                   '' as IdleStart,
                   '' as McStart,
                   0 as McQty
            FROM ShiftSummary sum
            JOIN ShiftScheduleSMT s ON sum.ShiftId = s.ShiftId
            JOIN Warehouses w ON sum.WarehouseId = w.WarehouseId
            WHERE CAST(s.ShiftDate AS DATE) = ? AND s.ShiftType = ?
        """;
        return jdbcTemplate.query(sql, this::mapRow, date, shiftType);
    }

    @Override
    public List<ShiftSummaryDTO> findByDate(LocalDate date) {
        String sql = """
            SELECT w.Name AS WarehouseName,
                   s.StartTime, s.EndTime,
                   sum.TorTimeSec, sum.TorQty, sum.TorPercent,
                   sum.PorTimeSec, sum.PorQty, sum.PorPercent,
                   sum.IdleTimeSec, sum.IdleQty, sum.IdlePercent,
                   '' as IdleStart,
                   '' as McStart,
                   0 as McQty
            FROM ShiftSummary sum
            JOIN ShiftScheduleSMT s ON sum.ShiftId = s.ShiftId
            JOIN Warehouses w ON sum.WarehouseId = w.WarehouseId
            WHERE CAST(s.ShiftDate AS DATE) = ?
        """;
        return jdbcTemplate.query(sql, this::mapRow, date);
    }

    @Override
    public List<ShiftSummaryDTO> findByShiftType(String shiftType) {
        String sql = """
            SELECT w.Name AS WarehouseName,
                   s.StartTime, s.EndTime,
                   sum.TorTimeSec, sum.TorQty, sum.TorPercent,
                   sum.PorTimeSec, sum.PorQty, sum.PorPercent,
                   sum.IdleTimeSec, sum.IdleQty, sum.IdlePercent,
                   '' as IdleStart,
                   '' as McStart,
                   0 as McQty
            FROM ShiftSummary sum
            JOIN ShiftScheduleSMT s ON sum.ShiftId = s.ShiftId
            JOIN Warehouses w ON sum.WarehouseId = w.WarehouseId
            WHERE s.ShiftType = ?
        """;
        return jdbcTemplate.query(sql, this::mapRow, shiftType);
    }

    @Override
    public List<ShiftSummaryDTO> findAllDTO() {
        String sql = """
            SELECT w.Name AS WarehouseName,
                   s.StartTime, s.EndTime,
                   sum.TorTimeSec, sum.TorQty, sum.TorPercent,
                   sum.PorTimeSec, sum.PorQty, sum.PorPercent,
                   sum.IdleTimeSec, sum.IdleQty, sum.IdlePercent,
                   '' as IdleStart,
                   '' as McStart,
                   0 as McQty
            FROM ShiftSummary sum
            JOIN ShiftScheduleSMT s ON sum.ShiftId = s.ShiftId
            JOIN Warehouses w ON sum.WarehouseId = w.WarehouseId
        """;
        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public List<ShiftSummaryDTO> findByDateShiftAndLines(LocalDate date, String shiftType, List<String> lineNames) {
        // Tạo placeholder ?, ?, ? theo số line
        String inSql = lineNames.stream().map(x -> "?").collect(Collectors.joining(","));

        String sql = """
        SELECT w.Name AS WarehouseName,
               s.StartTime, s.EndTime,
               sum.TorTimeSec, sum.TorQty, sum.TorPercent,
               sum.PorTimeSec, sum.PorQty, sum.PorPercent,
               sum.IdleTimeSec, sum.IdleQty, sum.IdlePercent,
               '' as IdleStart,
               '' as McStart,
               0 as McQty
        FROM ShiftSummary sum
        JOIN ShiftScheduleSMT s ON sum.ShiftId = s.ShiftId
        JOIN Warehouses w ON sum.WarehouseId = w.WarehouseId
        WHERE CAST(s.ShiftDate AS DATE) = ?
          AND s.ShiftType = ?
          AND w.Name IN (""" + inSql + ")";

        // build params (date, shiftType, + list lineNames)
        List<Object> params = new ArrayList<>();
        params.add(date);
        params.add(shiftType);
        params.addAll(lineNames);

        return jdbcTemplate.query(sql, this::mapRow, params.toArray());
    }




}
