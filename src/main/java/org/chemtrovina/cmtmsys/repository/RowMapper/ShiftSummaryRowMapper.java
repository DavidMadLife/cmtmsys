package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ShiftSummary;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ShiftSummaryRowMapper implements RowMapper<ShiftSummary> {
    @Override
    public ShiftSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        ShiftSummary summary = new ShiftSummary();
        summary.setSummaryId(rs.getLong("SummaryId"));
        summary.setShiftId(rs.getInt("ShiftId"));
        summary.setWarehouseId(rs.getInt("WarehouseId"));

        summary.setTotalTimeSec(rs.getInt("TotalTimeSec"));
        summary.setTorTimeSec(rs.getInt("TorTimeSec"));
        summary.setPorTimeSec(rs.getInt("PorTimeSec"));
        summary.setIdleTimeSec(rs.getInt("IdleTimeSec"));

        summary.setTorQty(rs.getInt("TorQty"));
        summary.setPorQty(rs.getInt("PorQty"));
        summary.setIdleQty(rs.getInt("IdleQty"));

        summary.setTorPercent(rs.getDouble("TorPercent"));
        summary.setPorPercent(rs.getDouble("PorPercent"));
        summary.setIdlePercent(rs.getDouble("IdlePercent"));

        summary.setMcPercent(rs.getDouble("McPercent"));
        summary.setMcQty(rs.getInt("McQty"));
        summary.setMcTimeSec(rs.getInt("McTimeSec"));

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) summary.setCreatedAt(created.toLocalDateTime());

        return summary;
    }
}
