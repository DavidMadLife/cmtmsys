package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialRowMapper;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MaterialRepositoryImpl implements MaterialRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Material material) {
        String sql = "INSERT INTO Materials (SapCode, RollCode, Quantity, WarehouseID, CreatedAt, Spec, EmployeeID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, material.getSapCode(), material.getRollCode(), material.getQuantity(),
                material.getWarehouseId(), material.getCreatedAt(), material.getSpec(), material.getEmployeeId());
    }

    @Override
    public void update(Material material) {
        String sql = "UPDATE Materials SET SapCode = ?, Quantity = ?, Spec = ?, EmployeeID = ?, WarehouseID = ? WHERE MaterialID = ?";
        jdbcTemplate.update(
                sql,
                material.getSapCode(),
                material.getQuantity(),
                material.getSpec(),
                material.getEmployeeId(),
                material.getWarehouseId(),
                material.getMaterialId()
        );
    }



    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM Materials WHERE MaterialID = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Material findById(int id) {
        String sql = "SELECT * FROM Materials WHERE MaterialID = ?";
        List<Material> results = jdbcTemplate.query(sql, new MaterialRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Material findByRollCode(String rollCode) {
        String sql = "SELECT * FROM Materials WHERE RollCode = ?";
        List<Material> results = jdbcTemplate.query(sql, new MaterialRowMapper(), rollCode);
        System.out.println(">>> DEBUG: Executing query with RollCode = " + rollCode);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Material> findAll() {
        return jdbcTemplate.query("SELECT * FROM Materials", new MaterialRowMapper());
    }

    @Override
    public List<Material> findByWarehouseId(int warehouseId) {
        String sql = "SELECT * FROM Materials WHERE WarehouseID = ?";
        return jdbcTemplate.query(sql, new MaterialRowMapper(), warehouseId);
    }

    @Override
    public List<Material> search(String sapCode, String barCode, LocalDateTime fromDate, LocalDateTime toDate, Integer warehouseId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Materials WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (sapCode != null && !sapCode.isEmpty()) {
            sql.append(" AND SapCode LIKE ?");
            params.add("%" + sapCode + "%");
        }
        if (barCode != null && !barCode.isEmpty()) {
            sql.append(" AND RollCode LIKE ?");
            params.add("%" + barCode + "%");
        }
        if (fromDate != null) {
            sql.append(" AND CreatedAt >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND CreatedAt <= ?");
            params.add(toDate);
        }
        if (warehouseId != null) {
            sql.append(" AND WarehouseID = ?");
            params.add(warehouseId);
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), new MaterialRowMapper());
    }

}
