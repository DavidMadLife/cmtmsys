package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.InvoiceDetailViewDto;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
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
    public Invoice findInvoicesByInvoicePN(String invoicePN) {
        String sql = "SELECT * FROM Invoice WHERE InvoicePN = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{invoicePN}, new InvoiceRowMapper());
    }

    public Invoice findInvoiceByInvoiceNo(String invoiceNo) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceNo = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{invoiceNo}, new InvoiceRowMapper());
    }


    public List<Invoice> findInvoicesByDateAndInvoiceNo(LocalDate date, String invoiceNo) {
        String sql = "SELECT * FROM Invoice WHERE InvoiceDate = ? AND InvoiceNo = ?";
        return jdbcTemplate.query(sql, new Object[]{date, invoiceNo}, new InvoiceRowMapper());
    }

    public List<String> findAllInvoiceNos() {
        String sql = "SELECT DISTINCT InvoiceNo FROM Invoice WHERE InvoiceNo IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findAllInvoicePNs() {
        String sql = "SELECT DISTINCT InvoicePN FROM Invoice WHERE InvoicePN IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }


    @Override
    public Invoice findInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM Invoice WHERE Id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{invoiceId}, new InvoiceRowMapper());
    }

    @Override
    public List<InvoiceDetail> getInvoiceDetailsByInvoiceId(int invoiceId) {
        String sql = "SELECT * FROM InvoiceDetail WHERE InvoiceId = ?";

        // Sử dụng JdbcTemplate để thực thi câu lệnh SQL và ánh xạ kết quả thành danh sách các đối tượng InvoiceDetail
        return jdbcTemplate.query(sql, new Object[]{invoiceId}, (rs, rowNum) -> {
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
    public int countHistoryByInvoiceId(int invoiceId) {
        String sql = "SELECT COUNT(*) FROM History WHERE InvoiceId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, invoiceId);
        return count != null ? count : 0;
    }


    @Override
    public void deleteInvoice(int invoiceId) {
        // 1. Kiểm tra xem có History không
        String checkHistorySql = "SELECT COUNT(*) FROM History WHERE InvoiceId = ?";
        Integer count = jdbcTemplate.queryForObject(checkHistorySql, Integer.class, invoiceId);

        if (count != null && count > 0) {
            throw new IllegalStateException("Cannot delete Invoice because it is referenced in History.");
        }

        // 2. Xóa chi tiết trước
        String deleteDetails = "DELETE FROM InvoiceDetail WHERE InvoiceId = ?";
        jdbcTemplate.update(deleteDetails, invoiceId);

        // 3. Xóa Invoice
        String deleteInvoice = "DELETE FROM Invoice WHERE Id = ?";
        jdbcTemplate.update(deleteInvoice, invoiceId);
    }


    @Override
    public List<InvoiceDetailViewDto> advancedSearch(String invoicePN, String sapPN, String makerPN, LocalDate date) {
        StringBuilder sql = new StringBuilder("""
        SELECT DISTINCT i.InvoiceNo, i.InvoicePN, i.InvoiceDate, d.SapPN, d.Quantity, d.MOQ, d.TotalReel
        FROM Invoice i
        JOIN InvoiceDetail d ON i.Id = d.InvoiceId
        LEFT JOIN MOQ m ON d.SapPN = m.SapPN
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (invoicePN != null && !invoicePN.trim().isEmpty()) {
            sql.append(" AND i.InvoicePN = ? ");
            params.add(invoicePN.trim());
        }
        if (sapPN != null && !sapPN.isEmpty()) {
            sql.append(" AND d.SapPN = ? ");
            params.add("%" + sapPN + "%");
        }
        if (makerPN != null && !makerPN.isEmpty()) {
            sql.append(" AND m.MakerPN = ? ");
            params.add("%" + makerPN + "%");
        }
        if (date != null) {
            sql.append(" AND i.InvoiceDate = ? ");
            params.add(date);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            InvoiceDetailViewDto dto = new InvoiceDetailViewDto();
            dto.setInvoiceNo(rs.getString("InvoiceNo"));
            dto.setInvoicePN(rs.getString("InvoicePN"));
            dto.setInvoiceDate(rs.getDate("InvoiceDate").toLocalDate());
            dto.setSapCode(rs.getString("SapPN"));
            dto.setQuantity(rs.getInt("Quantity"));
            dto.setMoq(rs.getInt("MOQ"));
            dto.setReelQty(rs.getInt("TotalReel"));
            return dto;
        });
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
        String invoiceSql = "INSERT INTO Invoice (InvoiceNo, InvoicePN, InvoiceDate, CreatedAt, Status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(invoiceSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, invoice.getInvoiceNo());
            ps.setString(2, invoice.getInvoicePN());
            ps.setDate(3, Date.valueOf(invoice.getInvoiceDate()));
            ps.setDate(4, Date.valueOf(invoice.getCreatedAt()));
            ps.setString(5, invoice.getStatus());
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
        String sql = "INSERT INTO Invoice (InvoiceNo, InvoicePN, InvoiceDate, CreatedAt, Status) VALUES (?, ?, ?, ?, ?)";
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

        if (invoice.getInvoicePN() != null && !invoice.getInvoicePN().isBlank()) {
            sql.append("InvoicePN = ?, ");
            params.add(invoice.getInvoicePN());
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
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{invoiceNo}, new InvoiceRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Invoice findByInvoicePN(String invoicePN) {
        String sql = "SELECT * FROM Invoice WHERE InvoicePN = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{invoicePN}, new InvoiceRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
                    rs.getString("InvoicePN"),
                    rs.getDate("InvoiceDate").toLocalDate(),
                    rs.getDate("CreatedAt").toLocalDate(),
                    rs.getString("Status")
            );
        }
    }
}
