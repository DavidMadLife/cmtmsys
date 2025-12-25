package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.repository.base.ShiftTypeEmployeeRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ShiftTypeEmployeeRepositoryImpl implements ShiftTypeEmployeeRepository {

    private final JdbcTemplate jdbc;
    private final BeanPropertyRowMapper<ShiftTypeEmployee> mapper =
            new BeanPropertyRowMapper<>(ShiftTypeEmployee.class);

    public ShiftTypeEmployeeRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ShiftTypeEmployee> findAll() {
        String sql = """
            SELECT *
            FROM ShiftTypeEmployee
        """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public ShiftTypeEmployee findByCode(String code) {
        String sql = """
            SELECT *
            FROM ShiftTypeEmployee
            WHERE ShiftCode = ?
        """;

        List<ShiftTypeEmployee> results = jdbc.query(sql, mapper, code);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public int insert(ShiftTypeEmployee type) {
        String sql = """
            INSERT INTO ShiftTypeEmployee(ShiftCode, ShiftName, Description)
            VALUES (?, ?, ?)
        """;
        return jdbc.update(sql, type.getShiftCode(), type.getShiftName(), type.getDescription());
    }

    @Override
    public int update(ShiftTypeEmployee type) {
        String sql = """
            UPDATE ShiftTypeEmployee
            SET ShiftName = ?, Description = ?
            WHERE ShiftCode = ?
        """;
        return jdbc.update(sql, type.getShiftName(), type.getDescription(), type.getShiftCode());
    }

    @Override
    public int delete(String shiftCode) {
        return jdbc.update("DELETE FROM ShiftTypeEmployee WHERE ShiftCode = ?", shiftCode);
    }
}
