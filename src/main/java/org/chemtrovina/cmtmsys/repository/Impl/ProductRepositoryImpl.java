package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(Product product) {
        String sql = "INSERT INTO Products (ProductCode, Description, CreatedDate, UpdatedDate) VALUES (?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                product.getProductCode(),
                product.getDescription(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    public void update(Product product) {
        String sql = "UPDATE Products SET ProductCode = ?, Description = ?, UpdatedDate = ? WHERE ProductID = ?";
        jdbcTemplate.update(sql,
                product.getProductCode(),
                product.getDescription(),
                Timestamp.valueOf(LocalDateTime.now()),
                product.getProductId()
        );
    }

    public void deleteById(int productId) {
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        jdbcTemplate.update(sql, productId);
    }

    public Product findById(int productId) {
        String sql = "SELECT * FROM Products WHERE ProductID = ?";
        List<Product> results = jdbcTemplate.query(sql, new ProductRowMapper(), productId);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Product> findAll() {
        String sql = "SELECT * FROM Products ORDER BY ProductID";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    @Override
    public Product getProductByCode(String code) {
        List<Product> result = jdbcTemplate.query(
                "SELECT * FROM Products WHERE productCode = ?",
                new Object[]{code},
                new ProductRowMapper()
        );
        return result.isEmpty() ? null : result.get(0);
    }

}
