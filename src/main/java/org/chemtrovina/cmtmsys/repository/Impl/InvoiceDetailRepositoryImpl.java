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
    public InvoiceDetail findBySapPNAndInvoiceId(String sapPN, int invoiceId) {
        String sql = "SELECT * FROM InvoiceDetail WHERE SapPN = ? AND InvoiceId = ?";
        List<InvoiceDetail> results = jdbcTemplate.query(sql, new Object[]{sapPN, invoiceId}, new InvoiceDetailRowMapper());

        return results.isEmpty() ? null : results.get(0);
    }



    @Override
    public void updateInvoiceDetail(int detailId, InvoiceDetail newDetail) {
        String sql = "UPDATE InvoiceDetail SET Quantity = ?, MOQ = ?, TotalReel = ?, Status = ? WHERE Id = ?";
        jdbcTemplate.update(sql, newDetail.getQuantity(), newDetail.getMoq(), newDetail.getTotalReel(), newDetail.getStatus(), detailId);
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
                    rs.getInt("InvoiceId"),
                    rs.getString("SapPN"),
                    rs.getInt("Quantity"),
                    rs.getInt("MOQ"),
                    rs.getString("Status"),
                    rs.getInt("TotalReel")
            );
        }
    }
}
