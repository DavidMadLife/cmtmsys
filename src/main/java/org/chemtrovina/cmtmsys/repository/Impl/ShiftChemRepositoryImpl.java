package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ShiftChem;
import org.chemtrovina.cmtmsys.repository.RowMapper.ShiftChemRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ShiftChemRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class ShiftChemRepositoryImpl implements ShiftChemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShiftChemRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ShiftChem shift) {
        String sql = "INSERT INTO ShiftChem (ShiftName) VALUES (?)";
        jdbcTemplate.update(sql, shift.getShiftName());
    }

    @Override
    public void update(ShiftChem shift) {
        String sql = "UPDATE ShiftChem SET ShiftName = ? WHERE ShiftId = ?";
        jdbcTemplate.update(sql, shift.getShiftName(), shift.getShiftId());
    }

    @Override
    public void deleteById(int shiftId) {
        String sql = "DELETE FROM ShiftChem WHERE ShiftId = ?";
        jdbcTemplate.update(sql, shiftId);
    }

    @Override
    public ShiftChem findById(int shiftId) {
        String sql = "SELECT * FROM ShiftChem WHERE ShiftId = ?";
        List<ShiftChem> results = jdbcTemplate.query(sql, new ShiftChemRowMapper(), shiftId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ShiftChem> findAll() {
        String sql = "SELECT * FROM ShiftChem";
        return jdbcTemplate.query(sql, new ShiftChemRowMapper());
    }
}
