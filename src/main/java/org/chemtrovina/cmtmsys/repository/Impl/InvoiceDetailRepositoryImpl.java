package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.base.InvoiceDetailRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDetailRepositoryImpl extends GenericRepositoryImpl<InvoiceDetail> implements InvoiceDetailRepository {

    public InvoiceDetailRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new InvoiceDetailRowMapper(), "InvoiceDetail");
    }

    @Override
    public void add(InvoiceDetail detail) {
        String sql = "INSERT INTO InvoiceDetail (InvoiceId, SapPN, Quantity, MOQ, Status, TotalReel) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                detail.getInvoiceId(),
                detail.getSapPN(),
                detail.getQuantity(),
                detail.getMoq(),
                detail.getStatus(),
                detail.getTotalReel());
    }

    @Override
    public void update(InvoiceDetail detail) {
        StringBuilder sql = new StringBuilder("UPDATE InvoiceDetail SET ");
        List<Object> params = new ArrayList<>();

        if (detail.getInvoiceId() != null && !detail.getInvoiceId().isBlank()) {
            sql.append("InvoiceId = ?, ");
            params.add(detail.getInvoiceId());
        }
        if (detail.getSapPN() != null && !detail.getSapPN().isBlank()) {
            sql.append("SapPN = ?, ");
            params.add(detail.getSapPN());
        }
        sql.append("Quantity = ?, MOQ = ?, Status = ?, TotalReel = ? ");
        params.add(detail.getQuantity());
        params.add(detail.getMoq());
        params.add(detail.getStatus());
        params.add(detail.getTotalReel());

        sql.append("WHERE Id = ?");
        params.add(detail.getId());

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public InvoiceDetail findById(int id) {
        String sql = "SELECT * FROM InvoiceDetail WHERE Id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new InvoiceDetailRowMapper());
    }

    @Override
    public List<InvoiceDetail> findAll() {
        String sql = "SELECT * FROM InvoiceDetail";
        return jdbcTemplate.query(sql, new InvoiceDetailRowMapper());
    }

    @Override
    public List<InvoiceDetail> findByInvoiceId(String invoiceId) {
        String sql = "SELECT * FROM InvoiceDetail WHERE InvoiceId = ?";
        return jdbcTemplate.query(sql, new Object[]{invoiceId}, new InvoiceDetailRowMapper());
    }

    static class InvoiceDetailRowMapper implements RowMapper<InvoiceDetail> {
        @Override
        public InvoiceDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new InvoiceDetail(
                    rs.getInt("Id"),
                    rs.getString("InvoiceId"),
                    rs.getString("SapPN"),
                    rs.getInt("Quantity"),
                    rs.getInt("MOQ"),
                    rs.getString("Status"),
                    rs.getInt("TotalReel")
            );
        }
    }
}
