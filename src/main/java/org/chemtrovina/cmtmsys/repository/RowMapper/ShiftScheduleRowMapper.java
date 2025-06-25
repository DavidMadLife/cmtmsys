package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.ShiftSchedule;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftScheduleRowMapper implements RowMapper<ShiftSchedule> {
    @Override
    public ShiftSchedule mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ShiftSchedule(
                rs.getInt("ID"),
                rs.getInt("EmployeeID"),
                rs.getDate("WorkDate").toLocalDate(),
                rs.getInt("ShiftID"),
                rs.getString("Note"),
                rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null
        );
    }
}
