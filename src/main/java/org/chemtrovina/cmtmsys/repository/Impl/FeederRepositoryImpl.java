package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Feeder;
import org.chemtrovina.cmtmsys.repository.RowMapper.FeederRowMapper;
import org.chemtrovina.cmtmsys.repository.base.FeederRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FeederRepositoryImpl implements FeederRepository {

    private final JdbcTemplate jdbcTemplate;

    public FeederRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Feeder feeder) {
        String sql = "INSERT INTO Feeders (ModelLineID, FeederCode, SapCode, Qty, Machine) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, feeder.getModelLineId(), feeder.getFeederCode(), feeder.getSapCode(), feeder.getQty(), feeder.getMachine());
    }

    @Override
    public void update(Feeder feeder) {
        String sql = "UPDATE Feeders SET Qty = ?, Machine = ? WHERE FeederID = ?";
        jdbcTemplate.update(sql, feeder.getQty(), feeder.getMachine(), feeder.getFeederId());
    }

    @Override
    public void deleteById(int feederId) {
        String sql = "DELETE FROM Feeders WHERE FeederID = ?";
        jdbcTemplate.update(sql, feederId);
    }

    @Override
    public Feeder findById(int feederId) {
        String sql = "SELECT * FROM Feeders WHERE FeederID = ?";
        List<Feeder> results = jdbcTemplate.query(sql, new FeederRowMapper(), feederId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Feeder> findAll() {
        String sql = "SELECT * FROM Feeders ORDER BY FeederID";
        return jdbcTemplate.query(sql, new FeederRowMapper());
    }

    @Override
    public List<Feeder> findByModelAndLine(int productId, int warehouseId) {
        String sql = """
            SELECT f.* FROM Feeders f
            JOIN ModelLines ml ON f.ModelLineID = ml.ModelLineID
            WHERE ml.ProductID = ? AND ml.WarehouseID = ?
        """;
        return jdbcTemplate.query(sql, new FeederRowMapper(), productId, warehouseId);
    }


    @Override
    public Feeder findByModelLineIdAndFeederCodeAndSapCode(int modelLineId, String feederCode, String sapCode) {
        String sql = "SELECT * FROM Feeders WHERE ModelLineID = ? AND FeederCode = ? AND SapCode = ?";
        List<Feeder> results = jdbcTemplate.query(sql, new FeederRowMapper(), modelLineId, feederCode, sapCode);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Feeder> search(int productId, int warehouseId, String feederCode, String sapCode) {
        String sql = """
            SELECT f.* FROM Feeders f
            JOIN ModelLines ml ON f.ModelLineID = ml.ModelLineID
            WHERE ml.ProductID = ?
              AND ml.WarehouseID = ?
              AND f.FeederCode LIKE ?
              AND f.SapCode LIKE ?
        """;
        return jdbcTemplate.query(sql, new FeederRowMapper(),
                productId, warehouseId,
                "%" + feederCode + "%", "%" + sapCode + "%"
        );
    }
}
