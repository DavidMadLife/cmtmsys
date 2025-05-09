package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoiceRepositoryImpl extends GenericRepositoryImpl<Invoice> implements InvoiceRepository {

    public InvoiceRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new InvoiceRowMapper(), "Invoice");
    }

    public List<Invoice> findInvoicesByDate(LocalDate date) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceDate = ?";
        return jdbcTemplate.query(sql, new Object[]{date}, new InvoiceRowMapper());
    }

    public List<Invoice> findInvoicesByInvoiceNo(String invoiceNo) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceNo = ?";
        return jdbcTemplate.query(sql, new Object[]{invoiceNo}, new InvoiceRowMapper());
    }

    public List<Invoice> findInvoicesByDateAndInvoiceNo(LocalDate date, String invoiceNo) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceDate = ? AND InvoiceNo = ?";
        return jdbcTemplate.query(sql, new Object[]{date, invoiceNo}, new InvoiceRowMapper());
    }

    public List<String> findAllInvoiceNos() {
        String sql = "SELECT DISTINCT InvoiceNo FROM Invoice";
        return jdbcTemplate.queryForList(sql, String.class);
    }



    @Override
    public void deleteInvoiceDetail(int invoiceId, String sapPN) {
        String sql = "DELETE FROM InvoiceDetail WHERE invoiceId = ? AND sapPN = ?";
        jdbcTemplate.update(sql, invoiceId, sapPN);
    }


    @Override
    public void updateInvoiceDetails(String invoiceNo, List<InvoiceDetail> details) {
        // ⚠️ Kiểm tra trùng sapPN trong danh sách details
        Set<String> uniqueSapCodes = new HashSet<>();
        for (InvoiceDetail detail : details) {
            if (!uniqueSapCodes.add(detail.getSapPN())) {
                throw new IllegalArgumentException("Duplicate SAP Code found: " + detail.getSapPN());
            }
        }

        // Tìm invoiceId theo invoiceNo
        String findInvoiceIdSql = "SELECT id FROM Invoice WHERE invoiceNo = ?";
        Long invoiceId = jdbcTemplate.queryForObject(findInvoiceIdSql, Long.class, invoiceNo);

        if (invoiceId == null) {
            throw new IllegalArgumentException("InvoiceNo not found: " + invoiceNo);
        }

        // Xoá detail cũ
        String deleteSql = "DELETE FROM InvoiceDetail WHERE invoiceId = ?";
        jdbcTemplate.update(deleteSql, invoiceId);

        // Insert lại
        String insertSql = "INSERT INTO InvoiceDetail (invoiceId, sapPN, quantity, moq, totalReel, status) VALUES (?, ?, ?, ?, ?, ?)";
        for (InvoiceDetail detail : details) {
            jdbcTemplate.update(insertSql,
                    invoiceId,
                    detail.getSapPN(),
                    detail.getQuantity(),
                    detail.getMoq(),
                    detail.getTotalReel(),
                    detail.getStatus());
        }
    }




    @Override
    public List<InvoiceDetail> getInvoiceDetails(String invoiceNo) {
        String sql = """
        SELECT d.id, d.invoiceId, d.sapPN, d.quantity, d.moq, d.totalReel, d.status
        FROM InvoiceDetail d
        JOIN Invoice i ON d.invoiceId = i.id
        WHERE i.invoiceNo = ?
        """;


        List<InvoiceDetail> details = jdbcTemplate.query(sql, new Object[]{invoiceNo}, (rs, rowNum) -> {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setId(rs.getInt("id"));
            detail.setInvoiceId(rs.getInt("invoiceId"));
            detail.setSapPN(rs.getString("sapPN"));
            detail.setQuantity(rs.getInt("quantity"));
            detail.setMoq(rs.getInt("moq"));
            detail.setTotalReel(rs.getInt("totalReel"));
            detail.setStatus(rs.getString("status"));
            return detail;
        });


        return details.isEmpty() ? null : details;
    }



    @Override
    public boolean existsByInvoiceNo(String invoiceNo) {
        String sql = "SELECT COUNT(*) FROM Invoice WHERE InvoiceNo = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, invoiceNo);
        return count != null && count > 0;
    }


    @Override
    public void saveInvoiceWithDetails(Invoice invoice, List<InvoiceDetail> details) {

        // Check trùng SapPN trong danh sách details
        Set<String> sapPNSet = new HashSet<>();
        for (InvoiceDetail detail : details) {
            if (!sapPNSet.add(detail.getSapPN())) {
                throw new IllegalArgumentException("SAP code '" + detail.getSapPN() + "' is existing in this invoice.");
            }
        }

        // Lưu invoice, lấy id sinh ra
        String invoiceSql = "INSERT INTO Invoice (InvoiceNo, InvoiceDate, CreatedAt, Status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(invoiceSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, invoice.getInvoiceNo());
            ps.setDate(2, Date.valueOf(invoice.getInvoiceDate()));
            ps.setDate(3, Date.valueOf(invoice.getCreatedAt()));
            ps.setString(4, invoice.getStatus());
            return ps;
        }, keyHolder);

        int invoiceId = keyHolder.getKey().intValue();
        invoice.setId(invoiceId);

        // Gán invoiceId vào từng detail rồi lưu
        String detailSql = "INSERT INTO InvoiceDetail (InvoiceId, SapPN, Quantity, MOQ, Status, TotalReel) VALUES (?, ?, ?, ?, ?, ?)";
        for (InvoiceDetail detail : details) {
            detail.setInvoiceId(invoiceId);
            jdbcTemplate.update(detailSql,
                    invoiceId,
                    detail.getSapPN(),
                    detail.getQuantity(),
                    detail.getMoq(),
                    detail.getStatus(),
                    detail.getTotalReel()
            );
        }
    }



    @Override
    public void add(Invoice invoice) {
        String sql = "INSERT INTO Invoice (InvoiceNo, InvoiceDate, CreatedAt, Status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                invoice.getInvoiceNo(),
                invoice.getInvoiceDate(),
                invoice.getCreatedAt(),
                invoice.getStatus());
    }

    @Override
    public void update(Invoice invoice) {
        StringBuilder sql = new StringBuilder("UPDATE Invoice SET ");
        List<Object> params = new ArrayList<>();

        if (invoice.getInvoiceNo() != null && !invoice.getInvoiceNo().isBlank()) {
            sql.append("InvoiceNo = ?, ");
            params.add(invoice.getInvoiceNo());
        }
        if (invoice.getInvoiceDate() != null) {
            sql.append("InvoiceDate = ?, ");
            params.add(invoice.getInvoiceDate());
        }
        if (invoice.getCreatedAt() != null) {
            sql.append("CreatedAt = ?, ");
            params.add(invoice.getCreatedAt());
        }
        if (invoice.getStatus() != null && !invoice.getStatus().isBlank()) {
            sql.append("Status = ?, ");
            params.add(invoice.getStatus());
        }

        if (params.isEmpty()) {
            return; // Không có trường nào để cập nhật
        }

        sql.setLength(sql.length() - 2); // Xoá dấu phẩy cuối
        sql.append(" WHERE Id = ?");
        params.add(invoice.getId());

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public Invoice findById(int id) {
        String sql = "SELECT * FROM Invoice WHERE Id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new InvoiceRowMapper());
    }

    @Override
    public List<Invoice> findAll() {
        String sql = "SELECT * FROM Invoice";
        return jdbcTemplate.query(sql, new InvoiceRowMapper());
    }

    @Override
    public Invoice findByInvoiceNo(String invoiceNo) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceNo = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{invoiceNo}, new InvoiceRowMapper());
    }

    @Override
    public List<Invoice> search(String invoiceNo, LocalDate invoiceDate, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Invoice WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (invoiceNo != null && !invoiceNo.isBlank()) {
            sql.append("AND InvoiceNo = ? ");
            params.add(invoiceNo);
        }
        if (invoiceDate != null) {
            sql.append("AND InvoiceDate = ? ");
            params.add(invoiceDate);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND Status = ? ");
            params.add(status);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), new InvoiceRowMapper());
    }

    static class InvoiceRowMapper implements RowMapper<Invoice> {
        @Override
        public Invoice mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Invoice(
                    rs.getInt("Id"),
                    rs.getString("InvoiceNo"),
                    rs.getDate("InvoiceDate").toLocalDate(),
                    rs.getDate("CreatedAt").toLocalDate(),
                    rs.getString("Status")
            );
        }
    }
}
