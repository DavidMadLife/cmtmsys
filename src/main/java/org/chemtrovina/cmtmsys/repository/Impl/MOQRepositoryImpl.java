package org.chemtrovina.cmtmsys.repository.Impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MOQRepositoryImpl extends GenericRepositoryImpl<MOQ> implements MOQRepository {

    private final JdbcTemplate jdbc;

    private final BeanPropertyRowMapper<MOQ> mapper =
            new BeanPropertyRowMapper<>(MOQ.class);

    public MOQRepositoryImpl(JdbcTemplate jdbcTemplate) {
        // ✅ nếu GenericRepositoryImpl bắt buộc truyền mapper + tableName thì vẫn truyền (nhưng bạn sẽ không dùng MOQRowMapper nữa)
        super(jdbcTemplate, new BeanPropertyRowMapper<>(MOQ.class), "MOQ");
        this.jdbc = jdbcTemplate;
    }

    // ========= Common SELECT with alias for BeanPropertyRowMapper =========
    private static final String SELECT_BASE = """
        SELECT
            Id       AS id,
            Maker    AS maker,
            MakerPN  AS makerPN,
            SapPN    AS sapPN,
            MOQ      AS moq,
            MSQL     AS msql,
            Spec     AS spec,
            CreatedAt AS createdAt,
            UpdatedAt AS updatedAt
        FROM MOQ
    """;

    //Find by SAP code
    @Override
    public MOQ findBySapPN(String sapPN) {
        String sql = SELECT_BASE + " WHERE SapPN = ?";
        List<MOQ> result = jdbc.query(sql, mapper, sapPN);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public MOQ findByMakerPN(String makerPN) {
        String sql = SELECT_BASE + " WHERE MakerPN = ?";
        List<MOQ> result = jdbc.query(sql, mapper, makerPN);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<MOQ> getAllMOQsByMakerPN(String makerPN) {
        String sql = SELECT_BASE + " WHERE MakerPN = ?";
        return jdbc.query(sql, mapper, makerPN);
    }

    @Override
    public List<MOQ> findAll() {
        String sql = SELECT_BASE;
        return jdbc.query(sql, mapper);
    }

    @Override
    public List<String> getAllSapCodes() {
        String sql = "SELECT DISTINCT SapPN FROM MOQ WHERE SapPN IS NOT NULL";
        return jdbc.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMakers() {
        String sql = "SELECT DISTINCT Maker FROM MOQ WHERE Maker IS NOT NULL";
        return jdbc.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMakerPNs() {
        String sql = "SELECT DISTINCT MakerPN FROM MOQ WHERE MakerPN IS NOT NULL";
        return jdbc.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMSLs() {
        String sql = "SELECT DISTINCT MSQL FROM MOQ WHERE MSQL IS NOT NULL";
        return jdbc.queryForList(sql, String.class);
    }

    @Override
    public List<String> findAllMakerPNs() {
        String sql = "SELECT DISTINCT MakerPN FROM MOQ WHERE MakerPN IS NOT NULL AND TRIM(MakerPN) <> ''";
        return jdbc.queryForList(sql, String.class);
    }

    // ===================== INSERT / UPDATE (auto set createdAt/updatedAt) =====================
    @Override
    public void add(MOQ moq) {
        // nếu UI không set time thì repo tự set
        LocalDateTime now = LocalDateTime.now();
        if (moq.getCreatedAt() == null) moq.setCreatedAt(now);
        if (moq.getUpdatedAt() == null) moq.setUpdatedAt(now);

        String sql = """
            INSERT INTO MOQ (Maker, MakerPN, SapPN, MOQ, MSQL, Spec, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbc.update(sql,
                moq.getMaker(),
                moq.getMakerPN(),
                moq.getSapPN(),
                moq.getMoq(),
                moq.getMsql(),
                moq.getSpec(),
                Timestamp.valueOf(moq.getCreatedAt()),
                Timestamp.valueOf(moq.getUpdatedAt())
        );
    }

    @Override
    public void update(MOQ moq) {
        // update thì luôn bump updatedAt
        moq.setUpdatedAt(LocalDateTime.now());

        String sql = """
            UPDATE MOQ
            SET Maker = ?, MakerPN = ?, SapPN = ?, MOQ = ?, MSQL = ?, Spec = ?, UpdatedAt = ?
            WHERE Id = ?
        """;

        jdbc.update(sql,
                moq.getMaker(),
                moq.getMakerPN(),
                moq.getSapPN(),
                moq.getMoq(),
                moq.getMsql(),
                moq.getSpec(),
                Timestamp.valueOf(moq.getUpdatedAt()),
                moq.getId()
        );
    }

    @Override
    public List<MOQ> searchMOQ(String maker, String makerPN, String sapPN, String MOQ, String MSL) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (maker != null && !maker.isBlank()) {
            sql.append(" AND Maker = ? ");
            params.add(maker);
        }
        if (makerPN != null && !makerPN.isBlank()) {
            sql.append(" AND MakerPN = ? ");
            params.add(makerPN);
        }
        if (sapPN != null && !sapPN.isBlank()) {
            sql.append(" AND SapPN = ? ");
            params.add(sapPN);
        }
        if (MOQ != null && !MOQ.isBlank()) {
            sql.append(" AND MOQ = ? ");
            params.add(Integer.parseInt(MOQ));
        }
        if (MSL != null && !MSL.isBlank()) {
            sql.append(" AND MSQL = ? ");
            params.add(MSL);
        }

        return jdbc.query(sql.toString(), mapper, params.toArray());
    }

    // ===================== Excel Import =====================
    @Override
    public List<MOQ> importMoqFromExcel(File file){
        List<MOQ> moqList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // bỏ header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                MOQ moq = new MOQ();
                moq.setSapPN(getCellValueAsString(row.getCell(0)));
                moq.setSpec(getCellValueAsString(row.getCell(1)));
                moq.setMakerPN(getCellValueAsString(row.getCell(2)));
                moq.setMaker(getCellValueAsString(row.getCell(3)));
                moq.setMoq((int) getCellValueAsNumeric(row.getCell(4)));
                moq.setMsql(getCellValueAsString(row.getCell(5)));

                // set time luôn nếu muốn
                LocalDateTime now = LocalDateTime.now();
                moq.setCreatedAt(now);
                moq.setUpdatedAt(now);

                moqList.add(moq);
            }

            System.out.println("Đọc được " + moqList.size() + " dòng từ Excel");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return moqList;
    }

    // ===================== Batch Save =====================
    @Override
    public void saveAll(List<MOQ> moqList) {
        String sql = """
            INSERT INTO MOQ (Maker, MakerPN, SapPN, MOQ, MSQL, Spec, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MOQ moq = moqList.get(i);

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime created = (moq.getCreatedAt() != null) ? moq.getCreatedAt() : now;
                LocalDateTime updated = (moq.getUpdatedAt() != null) ? moq.getUpdatedAt() : now;

                ps.setString(1, moq.getMaker());
                ps.setString(2, moq.getMakerPN());
                ps.setString(3, moq.getSapPN());
                ps.setInt(4, moq.getMoq() == null ? 0 : moq.getMoq());
                ps.setString(5, moq.getMsql());
                ps.setString(6, moq.getSpec());
                ps.setTimestamp(7, Timestamp.valueOf(created));
                ps.setTimestamp(8, Timestamp.valueOf(updated));
            }

            @Override
            public int getBatchSize() {
                return moqList.size();
            }
        });
    }

    // ===================== Update All (dynamic fields + UpdatedAt) =====================
    public void updateAll(List<MOQ> moqList) {
        for (MOQ moq : moqList) {
            StringBuilder sql = new StringBuilder("UPDATE MOQ SET ");
            List<Object> params = new ArrayList<>();

            if (moq.getMaker() != null && !moq.getMaker().isBlank()) {
                sql.append("Maker = ?, ");
                params.add(moq.getMaker());
            }
            if (moq.getMakerPN() != null && !moq.getMakerPN().isBlank()) {
                sql.append("MakerPN = ?, ");
                params.add(moq.getMakerPN());
            }
            if (moq.getSapPN() != null && !moq.getSapPN().isBlank()) {
                sql.append("SapPN = ?, ");
                params.add(moq.getSapPN());
            }
            if (moq.getMoq() != null && moq.getMoq() > 0) {
                sql.append("MOQ = ?, ");
                params.add(moq.getMoq());
            }
            if (moq.getMsql() != null && !moq.getMsql().isBlank()) {
                sql.append("MSQL = ?, ");
                params.add(moq.getMsql());
            }
            if (moq.getSpec() != null && !moq.getSpec().isBlank()) {
                sql.append("Spec = ?, ");
                params.add(moq.getSpec());
            }

            // luôn update UpdatedAt
            sql.append("UpdatedAt = ?, ");
            params.add(Timestamp.valueOf(LocalDateTime.now()));

            // nếu không có field nào (ngoài UpdatedAt) mà bạn muốn bỏ qua thì check size>1,
            // còn mình cho phép update UpdatedAt cũng OK.
            sql.setLength(sql.length() - 2); // remove last ", "
            sql.append(" WHERE Id = ?");
            params.add(moq.getId());

            jdbc.update(sql.toString(), params.toArray());
        }
    }

    // ===================== Helpers =====================
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }

    private double getCellValueAsNumeric(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue()); }
            catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }
}
