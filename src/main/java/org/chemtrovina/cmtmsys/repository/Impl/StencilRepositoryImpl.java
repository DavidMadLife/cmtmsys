package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Stencil;
import org.chemtrovina.cmtmsys.repository.RowMapper.StencilRowMapper;
import org.chemtrovina.cmtmsys.repository.base.StencilRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StencilRepositoryImpl implements StencilRepository {

    private final JdbcTemplate jdbcTemplate;

    public StencilRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Stencil s) {
        String sql = """
            INSERT INTO Stencils
            (Barcode, StencilNo, ProductId, CurrentWarehouseId, VersionLabel, Size,
             ArrayCount, ReceivedDate, Status, Note, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;

        // CurrentWarehouseId có thể NULL -> cần truyền type để JdbcTemplate setNull chuẩn
        Object[] args = {
                s.getBarcode(),
                s.getStencilNo(),
                s.getProductId(),
                s.getCurrentWarehouseId(),
                s.getVersionLabel(),
                s.getSize(),
                s.getArrayCount(),
                java.sql.Date.valueOf(s.getReceivedDate()),
                s.getStatus(),
                s.getNote()
        };
        int[] types = {
                Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.VARCHAR,
                Types.VARCHAR, Types.INTEGER, Types.DATE, Types.VARCHAR, Types.NVARCHAR
        };

        jdbcTemplate.update(sql, args, types);
    }

    @Override
    public void update(Stencil s) {
        String sql = """
            UPDATE Stencils SET
              Barcode = ?, StencilNo = ?, ProductId = ?, CurrentWarehouseId = ?,
              VersionLabel = ?, Size = ?, ArrayCount = ?, ReceivedDate = ?,
              Status = ?, Note = ?, UpdatedAt = GETDATE()
            WHERE StencilId = ?
        """;
        Object[] args = {
                s.getBarcode(),
                s.getStencilNo(),
                s.getProductId(),
                s.getCurrentWarehouseId(),
                s.getVersionLabel(),
                s.getSize(),
                s.getArrayCount(),
                java.sql.Date.valueOf(s.getReceivedDate()),
                s.getStatus(),
                s.getNote(),
                s.getStencilId()
        };
        int[] types = {
                Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
                Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.DATE,
                Types.VARCHAR, Types.NVARCHAR, Types.INTEGER
        };
        jdbcTemplate.update(sql, args, types);
    }

    @Override
    public void deleteById(int stencilId) {
        jdbcTemplate.update("DELETE FROM Stencils WHERE StencilId = ?", stencilId);
    }

    @Override
    public Stencil findById(int stencilId) {
        String sql = "SELECT * FROM Stencils WHERE StencilId = ?";
        List<Stencil> list = jdbcTemplate.query(sql, new StencilRowMapper(), stencilId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Stencil findByBarcode(String barcode) {
        String sql = "SELECT * FROM Stencils WHERE Barcode = ?";
        List<Stencil> list = jdbcTemplate.query(sql, new StencilRowMapper(), barcode);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Stencil> findAll() {
        return jdbcTemplate.query("SELECT * FROM Stencils ORDER BY StencilId", new StencilRowMapper());
    }

    @Override
    public List<Stencil> findByProductId(int productId) {
        String sql = "SELECT * FROM Stencils WHERE ProductId = ? ORDER BY StencilNo";
        return jdbcTemplate.query(sql, new StencilRowMapper(), productId);
    }

    @Override
    public List<Stencil> findByWarehouseId(Integer warehouseId) {
        // nếu muốn lấy những cái "đang trên máy" (CurrentWarehouseId IS NULL), gọi với warehouseId = null
        if (warehouseId == null) {
            String sqlNull = "SELECT * FROM Stencils WHERE CurrentWarehouseId IS NULL";
            return jdbcTemplate.query(sqlNull, new StencilRowMapper());
        }
        String sql = "SELECT * FROM Stencils WHERE CurrentWarehouseId = ?";
        return jdbcTemplate.query(sql, new StencilRowMapper(), warehouseId);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM Stencils WHERE Barcode = ?",
                Integer.class, barcode
        );
        return c != null && c > 0;
    }

    @Override
    public Stencil findByProductAndStencilNo(int productId, String stencilNo) {
        String sql = "SELECT * FROM Stencils WHERE ProductId = ? AND StencilNo = ?";
        List<Stencil> list = jdbcTemplate.query(sql, new StencilRowMapper(), productId, stencilNo);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void updateStatus(int stencilId, String status) {
        String sql = "UPDATE Stencils SET Status = ?, UpdatedAt = GETDATE() WHERE StencilId = ?";
        jdbcTemplate.update(sql, status, stencilId);
    }

    @Override
    public void transferWarehouse(int stencilId, Integer toWarehouseId) {
        String sql = "UPDATE Stencils SET CurrentWarehouseId = ?, UpdatedAt = GETDATE() WHERE StencilId = ?";
        if (toWarehouseId == null) {
            jdbcTemplate.update(sql, new Object[]{null, stencilId}, new int[]{Types.INTEGER, Types.INTEGER});
        } else {
            jdbcTemplate.update(sql, toWarehouseId, stencilId);
        }
    }

    @Override
    public List<StencilViewDto> findAllViews() {
        String sql = """
    SELECT s.StencilId, s.Barcode, s.StencilNo, s.ProductId,
    p.ProductCode, p.Name, p.ModelType, 
    s.VersionLabel, s.Size, s.ArrayCount, s.ReceivedDate,
    s.Status, w.Name AS Warehouse, s.Note
    FROM Stencils s
    JOIN Products p ON s.ProductId = p.ProductId
    LEFT JOIN Warehouses w ON s.CurrentWarehouseId = w.WarehouseId
    ORDER BY s.StencilId DESC
    """;


        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(StencilViewDto.class));
    }

    @Override
    public List<StencilViewDto> searchViews(String keyword, String productCode, String status, String warehouse) {
        StringBuilder sql = new StringBuilder("""
        SELECT s.StencilId, s.Barcode, s.StencilNo, s.ProductId,
               p.ProductCode, p.Name, p.ModelType, 
               s.VersionLabel, s.Size, s.ArrayCount, s.ReceivedDate,
               s.Status, w.Name AS Warehouse, s.Note
        FROM Stencils s
        JOIN Products p ON s.ProductId = p.ProductId
        LEFT JOIN Warehouses w ON s.CurrentWarehouseId = w.WarehouseId
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(s.Barcode) LIKE ? OR LOWER(s.StencilNo) LIKE ? OR LOWER(p.ProductCode) LIKE ? OR LOWER(p.Name) LIKE ?)");
            String kw = "%" + keyword.toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (productCode != null && !productCode.isBlank()) {
            sql.append(" AND p.ProductCode = ?");
            params.add(productCode);
        }

        if (status != null && !status.isBlank()) {
            sql.append(" AND s.Status = ?");
            params.add(status);
        }

        if (warehouse != null && !warehouse.isBlank()) {
            sql.append(" AND w.Name = ?");
            params.add(warehouse);
        }

        sql.append(" ORDER BY s.StencilId DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(StencilViewDto.class));
    }

}
