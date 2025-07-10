package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.RowMapper.WarehouseRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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

    public Warehouse findByName(String name) {
        String sql = "SELECT * FROM Warehouses WHERE Name = ?";
        List<Warehouse> results = jdbcTemplate.query(sql, new WarehouseRowMapper(), name);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public String getWarehouseNameByTransferId(int transferId, boolean isFrom) {
        String sql = isFrom
                ? "SELECT w.Name FROM WarehouseTransfers t JOIN Warehouses w ON t.FromWarehouseID = w.WarehouseID WHERE t.TransferID = ?"
                : "SELECT w.Name FROM WarehouseTransfers t JOIN Warehouses w ON t.ToWarehouseID = w.WarehouseID WHERE t.TransferID = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{transferId}, String.class);
        } catch (Exception e) {
            return "Không xác định";
        }
    }


}
