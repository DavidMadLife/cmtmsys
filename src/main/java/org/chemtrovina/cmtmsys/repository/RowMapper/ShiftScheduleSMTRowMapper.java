package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ShiftScheduleSMTRowMapper implements RowMapper<ShiftScheduleSMT> {
    @Override
    public ShiftScheduleSMT mapRow(ResultSet rs, int rowNum) throws SQLException {
        ShiftScheduleSMT shift = new ShiftScheduleSMT();
        shift.setShiftId(rs.getInt("ShiftId"));
        shift.setWarehouseId(rs.getInt("WarehouseId"));

        // shiftDate lưu trong DB là DATE → map sang LocalDateTime ở 00:00
        java.sql.Date date = rs.getDate("ShiftDate");
        if (date != null) shift.setShiftDate(date.toLocalDate().atStartOfDay());

        shift.setShiftType(rs.getString("ShiftType"));

        Timestamp start = rs.getTimestamp("StartTime");
        if (start != null) shift.setStartTime(start.toLocalDateTime());

        Timestamp end = rs.getTimestamp("EndTime");
        if (end != null) shift.setEndTime(end.toLocalDateTime());

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) shift.setCreatedAt(created.toLocalDateTime());

        return shift;
    }
}
