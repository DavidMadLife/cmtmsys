package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.SolderSession;
import org.chemtrovina.cmtmsys.repository.RowMapper.SolderSessionRowMapper;
import org.chemtrovina.cmtmsys.repository.base.SolderSessionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SolderSessionRepositoryImpl
        extends GenericRepositoryImpl<SolderSession>
        implements SolderSessionRepository {

    public SolderSessionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new SolderSessionRowMapper(), "SolderSession");
    }

    // ---------- CRUD ----------
    @Override
    public void add(SolderSession s) {
        String sql = """
            INSERT INTO SolderSession
              (SolderId, OutDate, AgingStartTime, AgingEndTime,
               WarehouseId, ReceiverEmployeeId, OpenTime,
               ReturnTime, ReturnEmployeeId, ScrapTime, ReturnStatus, Note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                s.getSolderId(),
                s.getOutDate(),
                s.getAgingStartTime(),
                s.getAgingEndTime(),
                s.getWarehouseId(),
                s.getReceiverEmployeeId(),
                s.getOpenTime(),
                s.getReturnTime(),
                s.getReturnEmployeeId(),
                s.getScrapTime(),
                s.getReturnStatus(),
                s.getNote()
        );
    }

    @Override
    public void update(SolderSession s) {
        String sql = """
            UPDATE SolderSession SET
               SolderId=?, OutDate=?, AgingStartTime=?, AgingEndTime=?,
               WarehouseId=?, ReceiverEmployeeId=?, OpenTime=?,
               ReturnTime=?, ReturnEmployeeId=?, ScrapTime=?, ReturnStatus=?, Note=?
            WHERE SessionId=?
        """;
        jdbcTemplate.update(sql,
                s.getSolderId(),
                s.getOutDate(),
                s.getAgingStartTime(),
                s.getAgingEndTime(),
                s.getWarehouseId(),
                s.getReceiverEmployeeId(),
                s.getOpenTime(),
                s.getReturnTime(),
                s.getReturnEmployeeId(),
                s.getScrapTime(),
                s.getReturnStatus(),
                s.getNote(),
                s.getSessionId()
        );
    }

    @Override
    public void deleteById(int sessionId) {
        jdbcTemplate.update("DELETE FROM SolderSession WHERE SessionId=?", sessionId);
    }

    @Override
    public SolderSession findById(int sessionId) {
        List<SolderSession> list = jdbcTemplate.query(
                "SELECT * FROM SolderSession WHERE SessionId=?",
                new SolderSessionRowMapper(), sessionId
        );
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SolderSession> findAll() {
        return jdbcTemplate.query("SELECT * FROM SolderSession ORDER BY SessionId DESC",
                new SolderSessionRowMapper());
    }

    @Override
    public List<SolderSession> findBySolderId(int solderId) {
        return jdbcTemplate.query(
                "SELECT * FROM SolderSession WHERE SolderId=? ORDER BY SessionId DESC",
                new SolderSessionRowMapper(), solderId
        );
    }

    // ---------- Business helpers ----------
    @Override
    public boolean existsActiveBySolderId(int solderId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM SolderSession WHERE SolderId=? AND ReturnTime IS NULL AND ScrapTime IS NULL",
                Integer.class, solderId
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public SolderSession findActiveBySolderId(int solderId) {
        List<SolderSession> list = jdbcTemplate.query("""
                SELECT TOP 1 * FROM SolderSession
                 WHERE SolderId=? AND ReturnTime IS NULL AND ScrapTime IS NULL
                 ORDER BY SessionId DESC
                """, new SolderSessionRowMapper(), solderId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void createOutSession(int solderId, LocalDate outDate,
                                 LocalDateTime agingStart, LocalDateTime agingEnd, String note) {
        String sql = """
            INSERT INTO SolderSession (SolderId, OutDate, AgingStartTime, AgingEndTime, Note)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, solderId, outDate, agingStart, agingEnd, note);
    }

    @Override
    public void markReceive(int sessionId, Integer warehouseId, Integer receiverEmployeeId, LocalDateTime openTime) {
        String sql = """
            UPDATE SolderSession
               SET WarehouseId=?, ReceiverEmployeeId=?, OpenTime=?
             WHERE SessionId=?
        """;
        jdbcTemplate.update(sql, warehouseId, receiverEmployeeId, openTime, sessionId);
    }

    @Override
    public void markReturnOK(int sessionId, Integer returnEmployeeId, LocalDateTime returnTime) {
        String sql = """
            UPDATE SolderSession
               SET ReturnTime=?, ReturnEmployeeId=?, ReturnStatus=N'OK'
             WHERE SessionId=?;
        """;
        jdbcTemplate.update(sql, returnTime, returnEmployeeId, sessionId);
    }

    @Override
    public void markScrap(int sessionId, LocalDateTime scrapTime) {
        String sql = """
            UPDATE SolderSession
               SET ScrapTime=?, ReturnStatus=COALESCE(ReturnStatus, N'SCRAP')
             WHERE SessionId=?;
        """;
        jdbcTemplate.update(sql, scrapTime, sessionId);
    }

    @Override
    public int autoScrapOverdue(int limitHours, LocalDateTime now) {
        String sql = """
            UPDATE SolderSession
               SET ScrapTime=?, ReturnStatus=COALESCE(ReturnStatus, N'SCRAP')
             WHERE OpenTime IS NOT NULL
               AND ReturnTime IS NULL
               AND ScrapTime IS NULL
               AND DATEADD(HOUR, ?, OpenTime) <= ?
        """;
        return jdbcTemplate.update(sql, now, limitHours, now);
    }

    // repository.Impl.SolderSessionRepositoryImpl
    @Override
    public void updatePartial(int sessionId, SolderSessionUpdate u) {
        String sql = """
        UPDATE dbo.SolderSession SET
            WarehouseId         = COALESCE(?, WarehouseId),
            ReceiverEmployeeId   = COALESCE(?, ReceiverEmployeeId),
            OpenTime             = COALESCE(?, OpenTime),
            ReturnEmployeeId     = COALESCE(?, ReturnEmployeeId),
            ReturnTime           = COALESCE(?, ReturnTime),
            ScrapTime            = COALESCE(?, ScrapTime),
            ReturnStatus         = COALESCE(?, ReturnStatus),
            Note                 = COALESCE(?, Note)
        WHERE SessionId = ?
        """;
        jdbcTemplate.update(sql,
                u.getWarehouseId(),
                u.getReceiverEmployeeId(),
                u.getOpenTime(),
                u.getReturnEmployeeId(),
                u.getReturnTime(),
                u.getScrapTime(),
                u.getReturnStatus(),
                u.getNote(),
                sessionId
        );
    }

}
