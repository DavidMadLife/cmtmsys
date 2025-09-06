package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.MaterialCart;
import org.chemtrovina.cmtmsys.repository.base.MaterialCartRepository;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialCartRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MaterialCartRepositoryImpl implements MaterialCartRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialCartRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(MaterialCart cart) {
        String sql = "INSERT INTO MaterialCarts (CartCode, CreatedAt) VALUES (?, ?)";
        jdbcTemplate.update(sql, cart.getCartCode(), cart.getCreatedAt());
    }

    @Override
    public List<MaterialCart> findAll() {
        return jdbcTemplate.query("SELECT * FROM MaterialCarts", new MaterialCartRowMapper());
    }

    @Override
    public MaterialCart findById(int cartId) {
        String sql = "SELECT * FROM MaterialCarts WHERE CartID = ?";
        return jdbcTemplate.queryForObject(sql, new MaterialCartRowMapper(), cartId);
    }

    @Override
    public MaterialCart findByCode(String cartCode) {
        String sql = "SELECT * FROM MaterialCarts WHERE CartCode = ?";
        return jdbcTemplate.queryForObject(sql, new MaterialCartRowMapper(), cartCode);
    }
}
