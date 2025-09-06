package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductBOMRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductBOMRepositoryImpl implements ProductBOMRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductBOMRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void update(ProductBOM bom) {
        String sql = "UPDATE ProductBOM SET SAPPN = ?, Quantity = ?, UpdatedDate = ? WHERE BomID = ?";
        jdbcTemplate.update(sql,
                bom.getSappn(),
                bom.getQuantity(),
                Timestamp.valueOf(LocalDateTime.now()),
                bom.getBomId()
        );
    }


    public void add(ProductBOM bom) {
        String sql = "INSERT INTO ProductBOM (ProductID, SAPPN, Quantity, CreatedDate, UpdatedDate) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                bom.getProductId(),
                bom.getSappn(),
                bom.getQuantity(),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    public void deleteByProductId(int productId) {
        String sql = "DELETE FROM ProductBOM WHERE ProductID = ?";
        jdbcTemplate.update(sql, productId);
    }

    public List<ProductBOM> findByProductId(int productId) {
        String sql = "SELECT * FROM ProductBOM WHERE ProductID = ?";
        return jdbcTemplate.query(sql, new ProductBOMRowMapper(), productId);
    }

    public List<ProductBomDto> findBomDtoByProductCode(String productCode) {
        String sql = """
        SELECT p.ProductCode, b.SAPPN, b.Quantity, 
               p.ModelType,
               FORMAT(b.CreatedDate, 'yyyy-MM-dd HH:mm:ss') AS CreatedDate,
               FORMAT(b.UpdatedDate, 'yyyy-MM-dd HH:mm:ss') AS UpdatedDate
        FROM ProductBOM b
        JOIN Products p ON p.ProductID = b.ProductID
        WHERE p.ProductCode = ?
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProductBomDto(
                rs.getString("ProductCode"),
                rs.getString("SAPPN"),
                rs.getDouble("Quantity"),
                rs.getString("ModelType"),
                rs.getTimestamp("CreatedDate").toLocalDateTime(),
                rs.getTimestamp("UpdatedDate").toLocalDateTime()
        ), productCode);
    }

    @Override
    public List<ProductBomDto> findBomDtoByProductCodeAndModelType(String productCode, String modelType) {
        String sql = """
        SELECT p.ProductCode, b.SAPPN, b.Quantity, 
               p.ModelType,
               FORMAT(b.CreatedDate, 'yyyy-MM-dd HH:mm:ss') AS CreatedDate,
               FORMAT(b.UpdatedDate, 'yyyy-MM-dd HH:mm:ss') AS UpdatedDate
        FROM ProductBOM b
        JOIN Products p ON p.ProductID = b.ProductID
        WHERE p.ProductCode = ? AND p.ModelType = ?
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProductBomDto(
                rs.getString("ProductCode"),
                rs.getString("SAPPN"),
                rs.getDouble("Quantity"),
                rs.getString("ModelType"),
                rs.getTimestamp("CreatedDate").toLocalDateTime(),
                rs.getTimestamp("UpdatedDate").toLocalDateTime()
        ), productCode, modelType);
    }

}
