package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Solder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolderRowMapper implements RowMapper<Solder> {

    @Override
    public Solder mapRow(ResultSet rs, int rowNum) throws SQLException {
        Solder s = new Solder();
        s.setSolderId(rs.getInt("SolderId"));
        s.setCode(rs.getString("Code"));
        s.setMaker(rs.getString("Maker"));
        s.setLot(rs.getString("Lot"));

        s.setReceivedDate(getLocalDate(rs, "ReceivedDate"));
        s.setMfgDate(getLocalDate(rs, "MfgDate"));
        s.setExpiryDate(getLocalDate(rs, "ExpiryDate"));
        s.setCreatedAt(getLocalDateTime(rs, "CreatedAt"));
        s.setViscotester(getNullableDouble(rs, "Viscotester"));
        return s;
    }

    private LocalDate getLocalDate(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getObject(col, LocalDate.class);
        } catch (Throwable ignore) {
            java.sql.Date d = rs.getDate(col);
            return d != null ? d.toLocalDate() : null;
        }
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        try {
            return rs.getObject(col, LocalDateTime.class);
        } catch (Throwable ignore) {
            Timestamp ts = rs.getTimestamp(col);
            return ts != null ? ts.toLocalDateTime() : null;
        }
    }
    private Double getNullableDouble(ResultSet rs, String col) throws SQLException {
        Object o = rs.getObject(col);          // null-safe cho FLOAT/DECIMAL
        return (o == null) ? null : ((Number) o).doubleValue();
        // Hoặc:
        // double v = rs.getDouble(col);
        // return rs.wasNull() ? null : v;
    }
}
