package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.StencilTransfer;
import org.chemtrovina.cmtmsys.repository.RowMapper.StencilTransferRowMapper;
import org.chemtrovina.cmtmsys.repository.base.StencilTransferRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class StencilTransferRepositoryImpl implements StencilTransferRepository {

    private final JdbcTemplate jdbcTemplate;

    public StencilTransferRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(StencilTransfer t) {
        String sql = """
            INSERT INTO StencilTransfers
              (StencilId, Barcode, FromWarehouseId, ToWarehouseId, TransferDate, PerformedBy, Note)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Object[] args = {
                t.getStencilId(),
                t.getBarcode(),
                t.getFromWarehouseId(),
                t.getToWarehouseId(),
                java.sql.Timestamp.valueOf(t.getTransferDate()),
                t.getPerformedBy(),
                t.getNote()
        };

        int[] types = {
                Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
                Types.TIMESTAMP, Types.VARCHAR, Types.NVARCHAR
        };

        jdbcTemplate.update(sql, args, types);
    }

    @Override
    public List<StencilTransfer> findByStencilId(int stencilId) {
        String sql = """
            SELECT * FROM StencilTransfers
            WHERE StencilId = ?
            ORDER BY TransferDate DESC
        """;
        return jdbcTemplate.query(sql, new StencilTransferRowMapper(), stencilId);
    }

    @Override
    public List<StencilTransfer> findAll() {
        String sql = "SELECT * FROM StencilTransfers ORDER BY TransferDate DESC";
        return jdbcTemplate.query(sql, new StencilTransferRowMapper());
    }

    @Override
    public void deleteById(int transferId) {
        String sql = "DELETE FROM StencilTransfers WHERE TransferId = ?";
        jdbcTemplate.update(sql, transferId);
    }
}
