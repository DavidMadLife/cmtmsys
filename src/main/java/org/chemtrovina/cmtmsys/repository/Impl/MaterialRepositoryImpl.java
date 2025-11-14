package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialRowMapper;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MaterialRepositoryImpl implements MaterialRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Material material) {
        String sql = """
            INSERT INTO Materials 
            (SapCode, RollCode, Quantity, WarehouseID, CreatedAt, Spec, EmployeeID, Lot, Maker)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                material.getSapCode(),
                material.getRollCode(),
                material.getQuantity(),
                material.getWarehouseId(),
                material.getCreatedAt(),
                material.getSpec(),
                material.getEmployeeId(),
                material.getLot(),
                material.getMaker() // ✅ mới
        );
    }

    @Override
    public void update(Material material) {
        Integer treeId = material.getTreeId();

        // Kiểm tra treeId có tồn tại không
        boolean treeExists = (treeId != null) && jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MaterialCartTrees WHERE TreeID = ?",
                Integer.class, treeId) > 0;

        if (treeExists) {
            String sql = """
                UPDATE Materials 
                SET SapCode = ?, Quantity = ?, Spec = ?, EmployeeID = ?, WarehouseID = ?, TreeID = ?, Lot = ?, Maker = ?
                WHERE MaterialID = ?
            """;
            jdbcTemplate.update(sql,
                    material.getSapCode(),
                    material.getQuantity(),
                    material.getSpec(),
                    material.getEmployeeId(),
                    material.getWarehouseId(),
                    treeId,
                    material.getLot(),
                    material.getMaker(), // ✅ mới
                    material.getMaterialId()
            );
        } else {
            String sql = """
                UPDATE Materials 
                SET SapCode = ?, Quantity = ?, Spec = ?, EmployeeID = ?, WarehouseID = ?, Lot = ?, Maker = ?
                WHERE MaterialID = ?
            """;
            jdbcTemplate.update(sql,
                    material.getSapCode(),
                    material.getQuantity(),
                    material.getSpec(),
                    material.getEmployeeId(),
                    material.getWarehouseId(),
                    material.getLot(),
                    material.getMaker(), // ✅ mới
                    material.getMaterialId()
            );
        }
    }

    @Override
    public List<Material> findByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        String inSql = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
        String sql = "SELECT * FROM Materials WHERE MaterialID IN (" + inSql + ")";
        return jdbcTemplate.query(sql, new MaterialRowMapper(), ids.toArray());
    }

    @Override
    public List<Material> findBySapCode(String sapCode) {
        String sql = "SELECT * FROM Materials WHERE SapCode = ?";
        return jdbcTemplate.query(sql, new MaterialRowMapper(), sapCode);
    }

    @Override
    public List<Material> getByTreeId(int treeId) {
        String sql = "SELECT * FROM Materials WHERE TreeID = ?";
        return jdbcTemplate.query(sql, new MaterialRowMapper(), treeId);
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

    @Override
    public void restore(int planItemId, int quantity) {
        String sql = """
            UPDATE Materials
            SET Quantity = Quantity + ?
            WHERE SapCode = (
                SELECT p.ProductCode
                FROM ProductionPlanItems ppi
                JOIN Products p ON ppi.ProductID = p.ProductID
                WHERE ppi.PlanItemID = ?
            )
        """;
        jdbcTemplate.update(sql, quantity, planItemId);
    }

    public void updateIgnoreTreeId(Material material) {
        String sql = "UPDATE Materials SET SapCode = ?, Quantity = ?, Spec = ?, EmployeeID = ?, Maker = ? WHERE MaterialID = ?";
        jdbcTemplate.update(
                sql,
                material.getSapCode(),
                material.getQuantity(),
                material.getSpec(),
                material.getEmployeeId(),
                material.getMaker(), // ✅ thêm mới
                material.getMaterialId()
        );
    }

    @Override
    public List<Material> findByRollCodes(List<String> rollCodes) {
        if (rollCodes == null || rollCodes.isEmpty()) return List.of();

        final int MAX_PARAMS = 1000; // Tránh vượt giới hạn 2100
        List<Material> result = new ArrayList<>();

        for (int i = 0; i < rollCodes.size(); i += MAX_PARAMS) {
            List<String> batch = rollCodes.subList(i, Math.min(i + MAX_PARAMS, rollCodes.size()));

            String placeholders = batch.stream().map(code -> "?").collect(Collectors.joining(", "));
            String sql = "SELECT * FROM Materials WHERE RollCode IN (" + placeholders + ")";

            result.addAll(jdbcTemplate.query(sql, batch.toArray(), new MaterialRowMapper()));
        }

        return result;
    }
}
