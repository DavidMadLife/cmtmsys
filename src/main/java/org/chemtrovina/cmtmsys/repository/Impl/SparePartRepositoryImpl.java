package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.SparePart;
import org.chemtrovina.cmtmsys.repository.RowMapper.SparePartRowMapper;
import org.chemtrovina.cmtmsys.repository.base.SparePartRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class SparePartRepositoryImpl implements SparePartRepository {

    private final JdbcTemplate jdbcTemplate;

    public SparePartRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(SparePart sp) {
        String sql = """
            INSERT INTO SpareParts
            ([Date], Name, Code, ImageData, Supplier, Manufacturer,
             Quantity, Unit, WarehouseKeeper, Note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql,
                sp.getDate(),
                sp.getName(),
                sp.getCode(),
                sp.getImageData(),
                sp.getSupplier(),
                sp.getManufacturer(),
                sp.getQuantity(),
                sp.getUnit(),
                sp.getWarehouseKeeper(),
                sp.getNote());
    }

    @Override
    public void update(SparePart sp) {
        String sql = """
        UPDATE SpareParts
        SET [Date]=?, Name=?, Code=?, ImageData=?, Supplier=?, Manufacturer=?,
            Quantity=?, Unit=?, WarehouseKeeper=?, Note=?
        WHERE Id=?
    """;

        jdbcTemplate.update(sql, ps -> {
            ps.setDate(1, java.sql.Date.valueOf(sp.getDate()));
            ps.setString(2, sp.getName());
            ps.setString(3, sp.getCode());
            ps.setBytes(4, sp.getImageData());
            ps.setString(5, sp.getSupplier());
            ps.setString(6, sp.getManufacturer());
            ps.setInt(7, sp.getQuantity());
            ps.setString(8, sp.getUnit());
            ps.setString(9, sp.getWarehouseKeeper());
            ps.setString(10, sp.getNote());
            ps.setInt(11, sp.getId());
        });
    }


    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM SpareParts WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public SparePart findById(int id) {
        String sql = "SELECT * FROM SpareParts WHERE Id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new SparePartRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<SparePart> findAll() {
        String sql = "SELECT * FROM SpareParts ORDER BY [Date] DESC, Id DESC";
        return jdbcTemplate.query(sql, new SparePartRowMapper());
    }

    @Override
    public List<SparePart> findByName(String name) {
        String sql = "SELECT * FROM SpareParts WHERE Name LIKE ? ORDER BY [Date] DESC";
        return jdbcTemplate.query(sql, new SparePartRowMapper(), "%" + name + "%");
    }

    @Override
    public SparePart findByCode(String code) {
        String sql = "SELECT * FROM SpareParts WHERE Code = ?";
        List<SparePart> list = jdbcTemplate.query(sql, new SparePartRowMapper(), code);
        return list.isEmpty() ? null : list.get(0);
    }

}
