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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Repository
public class MOQRepositoryImpl extends GenericRepositoryImpl<MOQ> implements MOQRepository {

    public MOQRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new MOQRowMapper(), "MOQ");
    }

    //Find by SAP code
    @Override
    public MOQ findBySapPN(String sapPN) {
        String sql = "SELECT * FROM MOQ WHERE SapPN = ?";
        List<MOQ> result = jdbcTemplate.query(sql, new MOQRowMapper(), sapPN);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<String> getAllSapCodes() {
        String sql = "SELECT DISTINCT SapPN FROM MOQ WHERE SapPN IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMakers() {
        String sql = "SELECT DISTINCT Maker FROM MOQ WHERE Maker IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMakerPNs() {
        String sql = "SELECT DISTINCT MakerPN FROM MOQ WHERE MakerPN IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<String> getAllMSLs() {
        String sql = "SELECT DISTINCT MSQL FROM MOQ WHERE MSQL IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public void add(MOQ moq) {
        String sql = "INSERT INTO MOQ (Maker, MakerPN, SapPN, MOQ, MSQL, Spec) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                moq.getMaker(),
                moq.getMakerPN(),
                moq.getSapPN(),
                moq.getMoq(),
                moq.getMsql(),
                moq.getSpec());
    }

    @Override
    public void update(MOQ moq) {
        String sql = "UPDATE MOQ SET Maker = ?, MakerPN = ?, SapPN = ?, MOQ = ?, MSQL = ?, Spec = ? WHERE Id = ?";
        jdbcTemplate.update(sql,
                moq.getMaker(),
                moq.getMakerPN(),
                moq.getSapPN(),
                moq.getMoq(),
                moq.getMsql(),
                moq.getSpec(),
                moq.getId());
    }

    @Override
    public List<MOQ> findAll() {
        String sql = "SELECT * FROM MOQ";
        return jdbcTemplate.query(sql, new MOQRowMapper());
    }

    @Override
    public List<String> findAllMakerPNs() {
        String sql = "SELECT DISTINCT MakerPN FROM MOQ WHERE MakerPN IS NOT NULL AND TRIM(MakerPN) <> ''";
        return jdbcTemplate.queryForList(sql, String.class);
    }


    @Override
    public List<MOQ> searchMOQ(String maker, String makerPN, String sapPN, String MOQ, String MSL) {
        StringBuilder sql = new StringBuilder("Select * FROM MOQ Where 1=1 ");
        List<Object> params = new java.util.ArrayList<>();
        if (maker != null && !maker.isBlank()) {
            sql.append("AND Maker = ? ");
            params.add(maker);
        }
        if (makerPN != null && !makerPN.isBlank()) {
            sql.append("AND MakerPN = ? ");
            params.add(makerPN);
        }
        if (sapPN != null && !sapPN.isBlank()) {
            sql.append("AND SapPN = ? ");
            params.add(sapPN);
        }
        if (MOQ != null && !MOQ.isBlank()) {
            sql.append("AND MOQ = ? ");
            params.add(Integer.parseInt(MOQ));
        }
        if (MSL != null && !MSL.isBlank()) {
            sql.append("AND MSQL = ? ");
            params.add(MSL);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), new  MOQRowMapper());
    }


    @Override
    public MOQ findByMakerPN(String makerPN) {
        String sql = "SELECT * FROM MOQ WHERE MakerPN = ?";
        List<MOQ> result = jdbcTemplate.query(sql, new MOQRowMapper(), makerPN);
        return result.isEmpty() ? null : result.get(0);
    }


    @Override
    public List<MOQ> importMoqFromExcel(File file){
        List<MOQ> moqList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ dòng tiêu đề
                Row row = sheet.getRow(i);
                if (row == null) continue;

                MOQ moq = new MOQ();
                moq.setSapPN(getCellValueAsString(row.getCell(0)));
                moq.setSpec(getCellValueAsString(row.getCell(1)));
                moq.setMaker(getCellValueAsString(row.getCell(3)));
                moq.setMakerPN(getCellValueAsString(row.getCell(2)));
                moq.setMoq((int) getCellValueAsNumeric(row.getCell(4)));
                moq.setMsql(getCellValueAsString(row.getCell(5)));

                moqList.add(moq);
            }
            System.out.println("Đọc được " + moqList.size() + " dòng từ Excel");

            //saveAll(moqList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return moqList;
    }


    @Override
    public List<MOQ> getAllMOQsByMakerPN(String makerPN) {
        String sql = "SELECT * FROM MOQ WHERE MakerPN = ?";
        return jdbcTemplate.query(sql, new MOQRowMapper(), makerPN);
    }


    //Save all
    @Override
    public void saveAll(List<MOQ> moqList) {
        String sql = "INSERT INTO MOQ (Maker, MakerPN, SapPN, MOQ, MSQL, Spec) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws SQLException {
                MOQ moq = moqList.get(i);
                ps.setString(1, moq.getMaker());
                ps.setString(2, moq.getMakerPN());
                ps.setString(3, moq.getSapPN());
                ps.setInt(4, moq.getMoq());
                ps.setString(5, moq.getMsql());
                ps.setString(6, moq.getSpec());
                System.out.printf("Saving MOQ row %d: SapPN=%s, MakerPN=%s, MOQ=%d%n",
                        i + 1,
                        moq.getSapPN(),
                        moq.getMakerPN(),
                        moq.getMoq());
            }

            @Override
            public int getBatchSize() {
                return moqList.size();
            }
        });
    }

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
            if (moq.getMoq() > 0) { // MOQ là int => kiểm tra khác 0 (tuỳ bạn quy định 0 là rỗng)
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

            // Nếu không có gì để update thì bỏ qua dòng này
            if (params.isEmpty()) continue;

            sql.setLength(sql.length() - 2); // Xoá dấu phẩy cuối
            sql.append(" WHERE Id = ?");
            params.add(moq.getId());

            jdbcTemplate.update(sql.toString(), params.toArray());
        }
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // nếu muốn hiển thị không có .0
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private double getCellValueAsNumeric(Cell cell) {
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }



    static class MOQRowMapper implements RowMapper<MOQ> {
        @Override
        public MOQ mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MOQ(
                    rs.getInt("Id"),
                    rs.getString("Maker"),
                    rs.getString("MakerPN"),
                    rs.getString("SapPN"),
                    rs.getInt("MOQ"),
                    rs.getString("MSQL"),
                    rs.getString("Spec")
            );
        }
    }
}
