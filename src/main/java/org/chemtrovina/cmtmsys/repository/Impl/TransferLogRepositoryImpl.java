package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.TransferLog;
import org.chemtrovina.cmtmsys.repository.RowMapper.TransferLogRowMapper;
import org.chemtrovina.cmtmsys.repository.base.TransferLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransferLogRepositoryImpl implements TransferLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransferLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(TransferLog log) {
        String sql = "INSERT INTO TransferLogs (RollCode, FromWarehouseID, ToWarehouseID, TransferDate, Note, EmployeeID) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                log.getRollCode(),
                log.getFromWarehouseId(),
                log.getToWarehouseId(),
                log.getTransferDate(),
                log.getNote(),
                log.getEmployeeId());
    }

    @Override
    public List<TransferLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM TransferLogs ORDER BY TransferDate DESC", new TransferLogRowMapper());
    }

    @Override
    public List<TransferLog> findByRollCode(String rollCode) {
        String sql = "SELECT * FROM TransferLogs WHERE RollCode = ? ORDER BY TransferDate DESC";
        return jdbcTemplate.query(sql, new TransferLogRowMapper(), rollCode);
    }

    @Override
    public List<TransferLog> search(String sapCode, String barcode, Integer fromWarehouseId, Integer toWarehouseId, LocalDateTime fromDate, LocalDateTime toDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT tl.* FROM TransferLogs tl " +
                        "JOIN Materials m ON tl.RollCode = m.RollCode " +
                        "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (sapCode != null && !sapCode.isBlank()) {
            sql.append(" AND LOWER(m.SapCode) LIKE ?");
            params.add("%" + sapCode.toLowerCase() + "%");
        }

        if (barcode != null && !barcode.isBlank()) {
            sql.append(" AND LOWER(tl.RollCode) LIKE ?");
            params.add("%" + barcode.toLowerCase() + "%");
        }

        if (fromWarehouseId != null) {
            sql.append(" AND tl.FromWarehouseID = ?");
            params.add(fromWarehouseId);
        }

        if (toWarehouseId != null) {
            sql.append(" AND tl.ToWarehouseID = ?");
            params.add(toWarehouseId);
        }

        if (fromDate != null) {
            sql.append(" AND tl.TransferDate >= ?");
            params.add(fromDate);
        }

        if (toDate != null) {
            sql.append(" AND tl.TransferDate <= ?");
            params.add(toDate);
        }

        sql.append(" ORDER BY tl.TransferDate DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), new TransferLogRowMapper());
    }

}
