package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.RowMapper.WarehouseRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final JdbcTemplate jdbcTemplate;

    public WarehouseRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Warehouse warehouse) {
        String sql = "INSERT INTO Warehouses (Name) VALUES (?)";
        jdbcTemplate.update(sql, warehouse.getName());
    }

    @Override
    public void update(Warehouse warehouse) {
        String sql = "UPDATE Warehouses SET Name = ? WHERE WarehouseID = ?";
        jdbcTemplate.update(sql, warehouse.getName(), warehouse.getWarehouseId());
    }

    @Override
    public void deleteById(int warehouseId) {
        String sql = "DELETE FROM Warehouses WHERE WarehouseID = ?";
        jdbcTemplate.update(sql, warehouseId);
    }

    @Override
    public Warehouse findById(int warehouseId) {
        String sql = "SELECT * FROM Warehouses WHERE WarehouseID = ?";
        List<Warehouse> results = jdbcTemplate.query(sql, new WarehouseRowMapper(), warehouseId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Warehouse> findAll() {
        String sql = "SELECT * FROM Warehouses ORDER BY WarehouseID";
        return jdbcTemplate.query(sql, new WarehouseRowMapper());
    }
}
