package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.SolderSession;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolderSessionRowMapper implements RowMapper<SolderSession> {

    @Override
    public SolderSession mapRow(ResultSet rs, int rowNum) throws SQLException {
        SolderSession s = new SolderSession();
        s.setSessionId(rs.getInt("SessionId"));
        s.setSolderId(rs.getInt("SolderId"));

        s.setOutDate(getLocalDate(rs, "OutDate"));
        s.setAgingStartTime(getLocalDateTime(rs, "AgingStartTime"));
        s.setAgingEndTime(getLocalDateTime(rs, "AgingEndTime"));

        int wh = rs.getInt("WarehouseId");
        s.setWarehouseId(rs.wasNull() ? null : wh);
        int recv = rs.getInt("ReceiverEmployeeId");
        s.setReceiverEmployeeId(rs.wasNull() ? null : recv);

        s.setOpenTime(getLocalDateTime(rs, "OpenTime"));
        s.setReturnTime(getLocalDateTime(rs, "ReturnTime"));
        int retEmp = rs.getInt("ReturnEmployeeId");
        s.setReturnEmployeeId(rs.wasNull() ? null : retEmp);

        s.setScrapTime(getLocalDateTime(rs, "ScrapTime"));
        s.setReturnStatus(rs.getString("ReturnStatus"));
        s.setNote(rs.getString("Note"));
        s.setCreatedAt(getLocalDateTime(rs, "CreatedAt"));
        return s;
    }

    private LocalDate getLocalDate(ResultSet rs, String col) throws SQLException {
        try { return rs.getObject(col, LocalDate.class); }
        catch (Throwable t) {
            java.sql.Date d = rs.getDate(col);
            return d != null ? d.toLocalDate() : null;
        }
    }
    private LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        try { return rs.getObject(col, LocalDateTime.class); }
        catch (Throwable t) {
            Timestamp ts = rs.getTimestamp(col);
            return ts != null ? ts.toLocalDateTime() : null;
        }
    }
}
