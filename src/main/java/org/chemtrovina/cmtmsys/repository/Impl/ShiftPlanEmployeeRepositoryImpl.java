package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;
import org.chemtrovina.cmtmsys.repository.base.ShiftPlanEmployeeRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ShiftPlanEmployeeRepositoryImpl implements ShiftPlanEmployeeRepository {

    private final JdbcTemplate jdbc;

    private final BeanPropertyRowMapper<ShiftPlanEmployee> mapper =
            new BeanPropertyRowMapper<>(ShiftPlanEmployee.class);

    public ShiftPlanEmployeeRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ============================================================
    @Override
    public List<ShiftPlanEmployee> findAll() {
        String sql = """
            SELECT 
                ShiftPlanId AS shiftPlanId,
                EmployeeId AS employeeId,
                ShiftDate AS shiftDate,
                ShiftCode AS shiftCode,
                Note AS note,
                ImportedBy AS importedBy,
                ImportedAt AS importedAt
            FROM ShiftPlanEmployee
            ORDER BY ShiftDate ASC
        """;
        return jdbc.query(sql, mapper);
    }

    // ============================================================
    @Override
    public List<ShiftPlanEmployee> findByEmployee(int employeeId) {
        String sql = """
            SELECT 
                ShiftPlanId AS shiftPlanId,
                EmployeeId AS employeeId,
                ShiftDate AS shiftDate,
                ShiftCode AS shiftCode,
                Note AS note,
                ImportedBy AS importedBy,
                ImportedAt AS importedAt
            FROM ShiftPlanEmployee
            WHERE EmployeeId = ?
            ORDER BY ShiftDate ASC
        """;
        return jdbc.query(sql, mapper, employeeId);
    }

    // ============================================================
    @Override
    public List<ShiftPlanEmployee> findByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT 
                ShiftPlanId AS shiftPlanId,
                EmployeeId AS employeeId,
                ShiftDate AS shiftDate,
                ShiftCode AS shiftCode,
                Note AS note,
                ImportedBy AS importedBy,
                ImportedAt AS importedAt
            FROM ShiftPlanEmployee
            WHERE EmployeeId = ?
              AND ShiftDate BETWEEN ? AND ?
            ORDER BY ShiftDate ASC
        """;
        return jdbc.query(sql, mapper, employeeId, from, to);
    }

    // ============================================================
    @Override
    public int insert(ShiftPlanEmployee p) {

        String sql = """
            INSERT INTO ShiftPlanEmployee(
                EmployeeId, ShiftDate, ShiftCode, Note, ImportedBy
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        return jdbc.update(
                sql,
                p.getEmployeeId(),
                p.getShiftDate(),
                p.getShiftCode(),
                p.getNote(),
                p.getImportedBy()
        );
    }

    // ============================================================
    @Override
    public void batchInsert(List<ShiftPlanEmployee> plans) {
        String sql = """
            INSERT INTO ShiftPlanEmployee(
                EmployeeId, ShiftDate, ShiftCode, Note, ImportedBy
            )
            VALUES (?, ?, ?, ?, ?)
        """;

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ShiftPlanEmployee p = plans.get(i);

                ps.setInt(1, p.getEmployeeId());
                ps.setDate(2, java.sql.Date.valueOf(p.getShiftDate()));
                ps.setString(3, p.getShiftCode());
                ps.setString(4, p.getNote());
                ps.setString(5, p.getImportedBy());
            }

            @Override
            public int getBatchSize() {
                return plans.size();
            }
        });
    }

    // ============================================================
    @Override
    public int delete(int shiftPlanId) {
        return jdbc.update("DELETE FROM ShiftPlanEmployee WHERE ShiftPlanId = ?", shiftPlanId);
    }

    @Override
    public int deleteByEmployee(int employeeId) {
        return jdbc.update("DELETE FROM ShiftPlanEmployee WHERE EmployeeId = ?", employeeId);
    }

    @Override
    public int deleteByDateRange(LocalDate from, LocalDate to) {
        return jdbc.update("DELETE FROM ShiftPlanEmployee WHERE ShiftDate BETWEEN ? AND ?", from, to);
    }

    @Override
    public int deleteByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to) {
        return jdbc.update("""
            DELETE FROM ShiftPlanEmployee
            WHERE EmployeeId = ? AND ShiftDate BETWEEN ? AND ?
        """, employeeId, from, to);
    }

    // ============================================================
    // ✓ FINAL – Save or Update
    // ============================================================
    @Override
    public int saveOrUpdate(int employeeId, LocalDate date, String shiftCode, String note) {

        String checkSql = """
            SELECT COUNT(*) FROM ShiftPlanEmployee
            WHERE EmployeeId = ? AND ShiftDate = ?
        """;

        Integer count = jdbc.queryForObject(checkSql, Integer.class, employeeId, date);

        // ========== UPDATE ==========
        if (count != null && count > 0) {
            String updateSql = """
                UPDATE ShiftPlanEmployee
                SET ShiftCode = ?, Note = ?
                WHERE EmployeeId = ? AND ShiftDate = ?
            """;

            return jdbc.update(updateSql,
                    shiftCode,
                    note,
                    employeeId,
                    date
            );
        }

        // ========== INSERT ==========
        String insertSql = """
            INSERT INTO ShiftPlanEmployee
            (EmployeeId, ShiftDate, ShiftCode, Note, ImportedBy)
            VALUES (?, ?, ?, ?, ?)
        """;

        return jdbc.update(insertSql,
                employeeId,
                date,
                shiftCode,
                note,
                "system"
        );
    }
}
