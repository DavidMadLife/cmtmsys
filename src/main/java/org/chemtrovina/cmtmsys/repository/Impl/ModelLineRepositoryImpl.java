package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ModelLine;
import org.chemtrovina.cmtmsys.repository.RowMapper.ModelLineRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModelLineRepositoryImpl implements ModelLineRepository {

    private final JdbcTemplate jdbcTemplate;

    public ModelLineRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ModelLine modelLine) {
        String sql = "INSERT INTO ModelLines (ProductID, WarehouseID) VALUES (?, ?)";
        jdbcTemplate.update(sql, modelLine.getProductId(), modelLine.getWarehouseId());
    }

    @Override
    public void update(ModelLine modelLine) {
        String sql = "UPDATE ModelLines SET ProductID = ?, WarehouseID = ? WHERE ModelLineID = ?";
        jdbcTemplate.update(sql, modelLine.getProductId(), modelLine.getWarehouseId(), modelLine.getModelLineId());
    }

    @Override
    public void deleteById(int modelLineId) {
        String sql = "DELETE FROM ModelLines WHERE ModelLineID = ?";
        jdbcTemplate.update(sql, modelLineId);
    }

    @Override
    public ModelLine findById(int modelLineId) {
        String sql = "SELECT * FROM ModelLines WHERE ModelLineID = ?";
        List<ModelLine> results = jdbcTemplate.query(sql, new ModelLineRowMapper(), modelLineId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ModelLine> findAll() {
        String sql = "SELECT * FROM ModelLines ORDER BY ModelLineID";
        return jdbcTemplate.query(sql, new ModelLineRowMapper());
    }

    @Override
    public ModelLine findOrCreateModelLine(int productId, int warehouseId) {
        String query = """
            SELECT * FROM ModelLines WHERE ProductID = ? AND WarehouseID = ?
        """;
        List<ModelLine> existing = jdbcTemplate.query(query, new ModelLineRowMapper(), productId, warehouseId);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // Tạo mới nếu chưa có
        String insert = "INSERT INTO ModelLines (ProductID, WarehouseID) VALUES (?, ?)";
        jdbcTemplate.update(insert, productId, warehouseId);

        // Truy vấn lại modelLine vừa tạo
        return jdbcTemplate.query(query, new ModelLineRowMapper(), productId, warehouseId).get(0);
    }
}
