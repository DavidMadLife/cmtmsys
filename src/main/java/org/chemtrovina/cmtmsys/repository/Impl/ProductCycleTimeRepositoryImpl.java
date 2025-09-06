package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.ProductCycleTimeViewDto;
import org.chemtrovina.cmtmsys.model.ProductCycleTime;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductCycleTimeRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductCycleTimeRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ProductCycleTimeRepositoryImpl implements ProductCycleTimeRepository {

    private final JdbcTemplate jdbc;

    public ProductCycleTimeRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(ProductCycleTime pct) {
        String sql = """
            INSERT INTO ProductCycleTime(ProductId, WarehouseId, CtSeconds, [Array], Active, Version, Note, CreatedAt)
            VALUES(?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbc.update(sql,
                pct.getProductId(),
                pct.getWarehouseId(),
                pct.getCtSeconds(),
                pct.getArray(),
                pct.isActive(),
                pct.getVersion(),
                pct.getNote(),
                Timestamp.valueOf(pct.getCreatedAt() != null ? pct.getCreatedAt() : LocalDateTime.now())
        );
    }

    @Override
    public ProductCycleTime getActive(int productId, int warehouseId) {
        String sql = """
            SELECT * FROM ProductCycleTime
            WHERE ProductId = ? AND WarehouseId = ? AND Active = 1
        """;
        List<ProductCycleTime> list = jdbc.query(sql, new ProductCycleTimeRowMapper(), productId, warehouseId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ProductCycleTime> findHistory(int productId, int warehouseId) {
        String sql = """
            SELECT * FROM ProductCycleTime
            WHERE ProductId = ? AND WarehouseId = ?
            ORDER BY CreatedAt DESC, CtId DESC
        """;
        return jdbc.query(sql, new ProductCycleTimeRowMapper(), productId, warehouseId);
    }

    @Override
    public ProductCycleTime findById(int ctId) {
        String sql = "SELECT * FROM ProductCycleTime WHERE CtId = ?";
        List<ProductCycleTime> list = jdbc.query(sql, new ProductCycleTimeRowMapper(), ctId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int deactivateAllFor(int productId, int warehouseId) {
        String sql = """
            UPDATE ProductCycleTime
            SET Active = 0
            WHERE ProductId = ? AND WarehouseId = ? AND Active = 1
        """;
        return jdbc.update(sql, productId, warehouseId);
    }

    @Override
    public int deleteById(int ctId) {
        String sql = "DELETE FROM ProductCycleTime WHERE CtId = ?";
        return jdbc.update(sql, ctId);
    }

    @Override
    public int deleteAllFor(int productId, int warehouseId) {
        String sql = "DELETE FROM ProductCycleTime WHERE ProductId = ? AND WarehouseId = ?";
        return jdbc.update(sql, productId, warehouseId);
    }

    @Override
    public List<ProductCycleTime> listActiveForProduct(int productId) {
        String sql = """
            SELECT * FROM ProductCycleTime
            WHERE ProductId = ? AND Active = 1
            ORDER BY WarehouseId
        """;
        return jdbc.query(sql, new ProductCycleTimeRowMapper(), productId);
    }

    @Override
    public List<ProductCycleTime> listActiveForWarehouse(int warehouseId) {
        String sql = """
            SELECT * FROM ProductCycleTime
            WHERE WarehouseId = ? AND Active = 1
            ORDER BY ProductId
        """;
        return jdbc.query(sql, new ProductCycleTimeRowMapper(), warehouseId);
    }

    /**
     * Đảm bảo 1 bản active duy nhất cho mỗi (productId, warehouseId)
     * 1) deactivate bản cũ
     * 2) insert bản mới active=true, [Array]=1 (mặc định), Version = MAX(Version)+1
     */
    @Override
    @Transactional
    public void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, String note) {
        setActiveCycleTime(productId, warehouseId, ctSeconds, 1, note);
    }

    /**
     * Đảm bảo 1 bản active duy nhất cho mỗi (productId, warehouseId)
     * 1) deactivate bản cũ
     * 2) insert bản mới active=true, với [Array] chỉ định, Version = MAX(Version)+1
     * Có xử lý DuplicateKeyException (filtered unique index)
     */
    @Override
    @Transactional
    public void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, int array, String note) {
        if (productId <= 0) throw new IllegalArgumentException("productId invalid");
        if (warehouseId <= 0) throw new IllegalArgumentException("warehouseId invalid");
        if (ctSeconds == null || ctSeconds.signum() <= 0) throw new IllegalArgumentException("ctSeconds must be > 0");
        if (array <= 0) throw new IllegalArgumentException("array must be > 0");

        // 1) deactivate bản cũ
        deactivateAllFor(productId, warehouseId);

        // 2) version = max+1
        Integer nextVersion = jdbc.queryForObject("""
                SELECT ISNULL(MAX(Version), 0) + 1
                FROM ProductCycleTime
                WHERE ProductId = ? AND WarehouseId = ?
            """, Integer.class, productId, warehouseId);
        if (nextVersion == null) nextVersion = 1;

        // 3) insert bản mới (active = true)
        try {
            jdbc.update("""
                INSERT INTO ProductCycleTime(ProductId, WarehouseId, CtSeconds, [Array], Active, Version, Note, CreatedAt)
                VALUES(?, ?, ?, ?, 1, ?, ?, SYSUTCDATETIME())
            """, productId, warehouseId, ctSeconds, array, nextVersion, note);
        } catch (DuplicateKeyException ex) {
            // race-condition: thử lại
            deactivateAllFor(productId, warehouseId);
            jdbc.update("""
                INSERT INTO ProductCycleTime(ProductId, WarehouseId, CtSeconds, [Array], Active, Version, Note, CreatedAt)
                VALUES(?, ?, ?, ?, 1, ?, ?, SYSUTCDATETIME())
            """, productId, warehouseId, ctSeconds, array, nextVersion, note);
        }
    }

    @Override
    public List<ProductCycleTimeViewDto> searchView(String productCodeLike,
                                                    ModelType modelTypeOrNull,
                                                    String lineNameLike) {
        String sql = """
        SELECT pct.CtId,
               p.ProductCode,
               p.ModelType,
               w.Name AS LineName,
               pct.[Array],
               pct.CtSeconds,
               pct.Version,
               pct.Active,
               pct.CreatedAt
        FROM ProductCycleTime pct
        JOIN Products  p ON p.ProductId = pct.ProductId
        JOIN Warehouses w ON w.WarehouseId = pct.WarehouseId
        WHERE (? = '' OR p.ProductCode LIKE ?)
          AND (? IS NULL OR p.ModelType = ?)
          AND (? = '' OR w.Name LIKE ?)
        ORDER BY p.ProductCode, w.Name, pct.Active DESC, pct.CreatedAt DESC
    """;

        String code = productCodeLike == null ? "" : productCodeLike.trim();
        String line = lineNameLike  == null ? "" : lineNameLike.trim();

        return jdbc.query(sql, ps -> {
            ps.setString(1, code);
            ps.setString(2, "%"+code+"%");
            ps.setString(3, modelTypeOrNull == null ? null : modelTypeOrNull.name());
            ps.setString(4, modelTypeOrNull == null ? null : modelTypeOrNull.name());
            ps.setString(5, line);
            ps.setString(6, "%"+line+"%");
        }, (rs, i) -> {
            ProductCycleTimeViewDto d = new ProductCycleTimeViewDto();
            d.setCtId(rs.getInt("CtId"));
            d.setProductCode(rs.getString("ProductCode"));
            d.setModelType(rs.getString("ModelType"));
            d.setLineName(rs.getString("LineName"));
            d.setArray(rs.getInt("Array"));
            d.setCtSeconds(rs.getBigDecimal("CtSeconds"));
            int v = rs.getInt("Version");
            d.setVersion(rs.wasNull() ? null : v);
            d.setActive(rs.getBoolean("Active"));
            d.setCreatedAt(rs.getObject("CreatedAt", java.time.LocalDateTime.class));
            return d;
        });
    }
}
