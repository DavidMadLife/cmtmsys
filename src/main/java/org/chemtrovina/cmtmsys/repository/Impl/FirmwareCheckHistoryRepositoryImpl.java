package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.FirmwareCheckHistory;
import org.chemtrovina.cmtmsys.repository.base.FirmwareCheckHistoryRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class FirmwareCheckHistoryRepositoryImpl implements FirmwareCheckHistoryRepository {

    private final JdbcTemplate jdbc;

    private final BeanPropertyRowMapper<FirmwareCheckHistory> mapper =
            new BeanPropertyRowMapper<>(FirmwareCheckHistory.class);

    public FirmwareCheckHistoryRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE = """
        SELECT
            Id AS id,
            InputVersion AS inputVersion,
            PopupVersion AS popupVersion,
            Result AS result,
            Message AS message,
            CreatedAt AS createdAt
        FROM FirmwareCheckHistory
    """;

    @Override
    public void insert(FirmwareCheckHistory h) {
        if (h.getCreatedAt() == null) h.setCreatedAt(LocalDateTime.now());

        String sql = """
            INSERT INTO FirmwareCheckHistory(InputVersion, PopupVersion, Result, Message, CreatedAt)
            VALUES (?, ?, ?, ?, ?)
        """;

        jdbc.update(sql,
                h.getInputVersion(),
                h.getPopupVersion(),
                h.getResult(),
                h.getMessage(),
                Timestamp.valueOf(h.getCreatedAt())
        );
    }

    @Override
    public List<FirmwareCheckHistory> findLatest(int top) {
        String sql = SELECT_BASE + " ORDER BY CreatedAt DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return jdbc.query(sql, mapper, top);
    }
}
