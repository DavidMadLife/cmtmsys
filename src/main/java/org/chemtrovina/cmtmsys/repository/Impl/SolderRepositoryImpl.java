package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Solder;
import org.chemtrovina.cmtmsys.repository.RowMapper.SolderRowMapper;
import org.chemtrovina.cmtmsys.repository.base.SolderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SolderRepositoryImpl extends GenericRepositoryImpl<Solder> implements SolderRepository {

    public SolderRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new SolderRowMapper(), "Solder");
    }

    // =========================
    // CRUD
    // =========================
    @Override
    public void add(Solder s) {
        String sql = """
            INSERT INTO Solder (Code, Maker, Lot, ReceivedDate, MfgDate, ExpiryDate, Viscotester)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                s.getCode(),
                s.getMaker(),
                s.getLot(),
                s.getReceivedDate(),
                s.getMfgDate(),
                s.getExpiryDate(),
                s.getViscotester()
        );
    }

    @Override
    public void update(Solder s) {
        String sql = """
            UPDATE Solder
               SET Code = ?,
                   Maker = ?,
                   Lot = ?,
                   ReceivedDate = ?,
                   MfgDate = ?,
                   ExpiryDate = ?,
                   Viscotester = ?
             WHERE SolderId = ?
        """;
        jdbcTemplate.update(sql,
                s.getCode(),
                s.getMaker(),
                s.getLot(),
                s.getReceivedDate(),
                s.getMfgDate(),
                s.getExpiryDate(),
                s.getViscotester(),
                s.getSolderId()
        );
    }

    @Override
    public void deleteById(int solderId) {
        String sql = "DELETE FROM Solder WHERE SolderId = ?";
        jdbcTemplate.update(sql, solderId);
    }

    // =========================
    // Queries
    // =========================
    @Override
    public Solder findById(int solderId) {
        String sql = "SELECT * FROM Solder WHERE SolderId = ?";
        List<Solder> list = jdbcTemplate.query(sql, rowMapper, solderId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Solder findByCode(String code) {
        String sql = "SELECT * FROM Solder WHERE Code = ?";
        List<Solder> list = jdbcTemplate.query(sql, rowMapper, code);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Solder> findAll() {
        String sql = "SELECT * FROM Solder ORDER BY SolderId DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(1) FROM Solder WHERE Code = ?";
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return cnt != null && cnt > 0;
    }

    @Override
    public List<Solder> search(String code,
                               String maker,
                               String lot,
                               LocalDate receivedFrom,
                               LocalDate receivedTo,
                               LocalDate expiryFrom,
                               LocalDate expiryTo) {
        StringBuilder sb = new StringBuilder("SELECT * FROM Solder WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (code != null && !code.isBlank()) {
            sb.append(" AND Code = ?");
            params.add(code.trim());
        }
        if (maker != null && !maker.isBlank()) {
            sb.append(" AND Maker LIKE ?");
            params.add("%" + maker.trim() + "%");
        }
        if (lot != null && !lot.isBlank()) {
            sb.append(" AND Lot LIKE ?");
            params.add("%" + lot.trim() + "%");
        }
        if (receivedFrom != null) {
            sb.append(" AND ReceivedDate >= ?");
            params.add(receivedFrom);
        }
        if (receivedTo != null) {
            sb.append(" AND ReceivedDate <= ?");
            params.add(receivedTo);
        }
        if (expiryFrom != null) {
            sb.append(" AND ExpiryDate >= ?");
            params.add(expiryFrom);
        }
        if (expiryTo != null) {
            sb.append(" AND ExpiryDate <= ?");
            params.add(expiryTo);
        }

        sb.append(" ORDER BY SolderId DESC");

        return jdbcTemplate.query(sb.toString(), rowMapper, params.toArray());
    }

    @Override
    public List<Solder> findExpiringBetween(LocalDate from, LocalDate to) {
        String sql = """
            SELECT * FROM Solder
             WHERE ExpiryDate IS NOT NULL
               AND ExpiryDate BETWEEN ? AND ?
             ORDER BY ExpiryDate ASC, SolderId DESC
        """;
        return jdbcTemplate.query(sql, rowMapper, from, to);
    }

    private static String escapeLike(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")  // escape backslash trước
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    @Override
    public List<Solder> searchByCode(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) return List.of();
        if (limit <= 0) limit = 20;

        String pattern = "%" + escapeLike(keyword.trim()) + "%";

        String sql = """
        SELECT *
          FROM Solder
         WHERE LOWER(Code) LIKE LOWER(?) ESCAPE '\\'
         ORDER BY Code ASC, SolderId DESC
         OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
    """;

        return jdbcTemplate.query(sql, rowMapper, pattern, limit);
    }



    @Override
    public List<String> suggestCodes(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        if (limit <= 0) {
            limit = 100;
        }
        String pattern = "%" + escapeLike(keyword) + "%";

        String sql = """
                Select distinct Code from Solder
                Where Code LIKE LOWER(?) ESCAPE '\\'
                OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
                """;
        return jdbcTemplate.query(sql, (rs, i) -> rs.getString(1), pattern, limit);
    }

}
