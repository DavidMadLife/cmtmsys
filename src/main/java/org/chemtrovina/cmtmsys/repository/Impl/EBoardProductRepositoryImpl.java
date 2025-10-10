package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.EBoardProduct;
import org.chemtrovina.cmtmsys.repository.base.EBoardProductRepository;
import org.chemtrovina.cmtmsys.repository.RowMapper.EBoardProductRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EBoardProductRepositoryImpl implements EBoardProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public EBoardProductRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(EBoardProduct product) {
        String sql = """
            INSERT INTO EBoardProducts (SetId, ProductId, CircuitType, Description)
            VALUES (?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                product.getSetId(),
                product.getProductId(),
                product.getCircuitType(),
                product.getDescription());
    }

    @Override
    public void update(EBoardProduct product) {
        String sql = """
            UPDATE EBoardProducts
            SET SetId = ?, ProductId = ?, CircuitType = ?, Description = ?
            WHERE Id = ?
        """;
        jdbcTemplate.update(sql,
                product.getSetId(),
                product.getProductId(),
                product.getCircuitType(),
                product.getDescription(),
                product.getId());
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM EBoardProducts WHERE Id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public EBoardProduct findById(int id) {
        String sql = "SELECT * FROM EBoardProducts WHERE Id = ?";
        return jdbcTemplate.query(sql, new EBoardProductRowMapper(), id)
                .stream().findFirst().orElse(null);
    }

    @Override
    public List<EBoardProduct> findBySet(int setId) {
        String sql = "SELECT * FROM EBoardProducts WHERE SetId = ?";
        return jdbcTemplate.query(sql, new EBoardProductRowMapper(), setId);
    }

    @Override
    public List<EBoardProduct> findBySetAndCircuit(int setId, String circuitType) {
        String sql = "SELECT * FROM EBoardProducts WHERE SetId = ? AND CircuitType = ?";
        return jdbcTemplate.query(sql, new EBoardProductRowMapper(), setId, circuitType);
    }

    @Override
    public List<EBoardProduct> findAll() {
        String sql = "SELECT * FROM EBoardProducts ORDER BY SetId";
        return jdbcTemplate.query(sql, new EBoardProductRowMapper());
    }
}
