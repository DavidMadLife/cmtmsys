package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.repository.base.HistoryRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepositoryImpl extends GenericRepositoryImpl<History> implements HistoryRepository {

    public HistoryRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new HistoryRowMapper(), "History"); // "History" là tên bảng trong database
    }



    @Override
    public void add(History history) {
        String sql = "INSERT INTO History (InvoiceId, Date, Time, Maker, MakerPN, SapPN, Quantity, EmployeeId, Status, ScanCode, MSL, InvoicePN, Spec) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                history.getInvoiceId(),
                history.getDate(),
                history.getTime(),
                history.getMaker(),
                history.getMakerPN(),
                history.getSapPN(),
                history.getQuantity(),
                history.getEmployeeId(),
                history.getStatus(),
                history.getScanCode(),
                history.getMSL(),
                history.getInvoicePN(),
                history.getSpec()
        );
    }


    @Override
    public void update(History history) {
        // Xây dựng câu lệnh SQL động để cập nhật chỉ những trường có thay đổi
        StringBuilder sql = new StringBuilder("UPDATE History SET ");
        List<Object> params = new ArrayList<>();
        if (history.getDate() != null) {
            sql.append("Date = ?, ");
            params.add(history.getDate());
        }
        if (history.getTime() != null) {
            sql.append("Time = ?, ");
            params.add(history.getTime());
        }
        if (history.getMaker() != null && !history.getMaker().equals("")) {
            sql.append("Maker = ?, ");
            params.add(history.getMaker());
        }
        if (history.getMakerPN() != null && !history.getMakerPN().equals("")) {
            sql.append("MakerPN = ?, ");
            params.add(history.getMakerPN());
        }
        if (history.getSapPN() != null && !history.getSapPN().equals("")) {
            sql.append("SapPN = ?, ");
            params.add(history.getSapPN());
        }
        if (history.getQuantity() > 0) {
            sql.append("Quantity = ?, ");
            params.add(history.getQuantity());
        }
        if (history.getEmployeeId() != null) {
            sql.append("EmployeeId = ?, ");
            params.add(history.getEmployeeId());
        }
        if (history.getStatus() != null && !history.getStatus().equals("")) {
            sql.append("Status = ?, ");
            params.add(history.getStatus());
        }
        if (history.getScanCode() != null && !history.getScanCode().equals("")) {
            sql.append("ScanCode = ?, ");
            params.add(history.getScanCode());
        }
        if (history.getMSL() != null && !history.getMSL().equals("")) {
            sql.append("MSL = ?, ");
            params.add(history.getMSL());
        }

        if (history.getSpec() != null && !history.getSpec().equals("")) {
            sql.append("Spec = ?, ");
            params.add(history.getSpec());
        }

        // Loại bỏ dấu phẩy cuối câu SQL
        sql.setLength(sql.length() - 2);

        // Thêm phần WHERE vào câu SQL
        sql.append(" WHERE Id = ?");
        params.add(history.getId());

        // Chạy câu SQL với các tham số
        jdbcTemplate.update(sql.toString(), params.toArray());
    }


    @Override
    public List<History> findAll() {
        String sql = "SELECT * FROM History";
        return jdbcTemplate.query(sql, new HistoryRowMapper());
    }

    @Override
    public History findById(int id) {
        String sql = "SELECT * FROM History WHERE Id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new HistoryRowMapper());
    }

    @Override
    public List<History> search(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL, String InvoicePN) {
        StringBuilder sql = new StringBuilder(
                "SELECT h.* FROM History h LEFT JOIN Invoice i ON h.InvoiceId = i.Id WHERE 1=1 "
        );

        // Danh sách parameter
        List<Object> params = new java.util.ArrayList<>();

        if (invoiceNo != null && !invoiceNo.isBlank()) {
            sql.append("AND i.InvoiceNo = ? ");
            params.add(invoiceNo);
        }

        if (maker != null && !maker.isBlank()) {
            sql.append("AND h.Maker = ? ");
            params.add(maker);
        }

        if (makerPN != null && !makerPN.isBlank()) {
            sql.append("AND h.MakerPN = ? ");
            params.add(makerPN);
        }

        if (sapPN != null && !sapPN.isBlank()) {
            sql.append("AND h.SapPN = ? ");
            params.add(sapPN);
        }

        if (date != null) {
            sql.append("AND h.Date = ? ");
            params.add(date);
        }
        if (MSL != null) {
            sql.append("AND h.MSL = ? ");
            params.add(MSL);
        }

        if (InvoicePN != null) {
            sql.append("AND h.InvoicePN = ? ");
            params.add(InvoicePN);
        }
        List<History> list = jdbcTemplate.query(sql.toString(), params.toArray(), new HistoryRowMapper());

        for (History history : list) {
            if (history.getInvoiceId() != null) {
                try {
                    String invNo = jdbcTemplate.queryForObject(
                            "SELECT InvoiceNo FROM Invoice WHERE Id = ?",
                            new Object[]{history.getInvoiceId()},
                            String.class
                    );
                    history.setInvoiceNo(invNo);
                } catch (EmptyResultDataAccessException e) {
                    // Không tìm thấy invoice -> giữ nguyên hoặc log cảnh báo
                    System.out.println("Không tìm thấy InvoiceNo cho InvoiceId = " + history.getInvoiceId());
                }

            }
        }

        return list;
    }

    @Override
    public boolean existsByScanCodeAndMakerPN(String scanCode, String makerPN) {
        String sql = "SELECT COUNT(*) FROM History WHERE ScanCode = ? AND MakerPN = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, scanCode, makerPN);
        return count != null && count > 0;
    }

    @Override
    public List<History> findByInvoiceId(int invoiceId) {
        String sql = "SELECT * FROM History WHERE InvoiceId = ?";
        return jdbcTemplate.query(sql, new Object[]{invoiceId}, new HistoryRowMapper());
    }


    @Override
    public int getTotalScannedQuantityBySapPN(String sapPN, int invoiceId) {
        String sql = "SELECT SUM(Quantity) FROM History WHERE SapPN = ? AND InvoiceId = ?";
        Integer totalQuantity = jdbcTemplate.queryForObject(sql, new Object[]{sapPN, invoiceId}, Integer.class);
        return totalQuantity != null ? totalQuantity : 0; // Trả về 0 nếu không có bản ghi nào
    }


    @Override
    public void deleteLastByMakerPNAndInvoiceId(String makerPN, int invoiceId) {
        String sql = "SELECT TOP 1 Id FROM History WHERE MakerPN = ? AND InvoiceId = ? ORDER BY Date DESC, Time DESC";


        List<Integer> ids = jdbcTemplate.query(sql, new Object[]{makerPN, invoiceId},
                (rs, rowNum) -> rs.getInt("Id"));

        if (!ids.isEmpty()) {
            int idToDelete = ids.get(0);
            String sqlDelete = "DELETE FROM History WHERE Id = ?";
            jdbcTemplate.update(sqlDelete, idToDelete);
        }
    }

    static class HistoryRowMapper implements RowMapper<History> {
        @Override
        public History mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new History(
                    rs.getInt("Id"),
                    rs.getInt("InvoiceId"),
                    rs.getDate("Date").toLocalDate(),
                    rs.getTime("Time").toLocalTime(),
                    rs.getString("Maker"),
                    rs.getString("MakerPN"),
                    rs.getString("SapPN"),
                    rs.getInt("Quantity"),
                    rs.getString("EmployeeId"),
                    rs.getString("Status"),
                    rs.getString("ScanCode"),
                    rs.getString("MSL"),
                    rs.getString("InvoicePN"),
                    rs.getString("Spec")
            );
        }
    }
}
