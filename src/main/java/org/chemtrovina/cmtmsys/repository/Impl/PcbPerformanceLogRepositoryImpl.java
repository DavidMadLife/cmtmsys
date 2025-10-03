package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.RowMapper.PcbPerformanceLogRowMapper;
import org.chemtrovina.cmtmsys.repository.base.PcbPerformanceLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PcbPerformanceLogRepositoryImpl implements PcbPerformanceLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public PcbPerformanceLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int add(PcbPerformanceLog log) {
        String sql = """
        INSERT INTO PcbPerformanceLog
        (ProductId, WarehouseId, CarrierId, AoiMachineCode, TotalModules, NgModules, Performance, LogFileName, CreatedAt)
        OUTPUT INSERTED.LogId
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        Integer id = jdbcTemplate.queryForObject(sql, Integer.class,
                log.getProductId(),
                log.getWarehouseId(),
                log.getCarrierId(),
                log.getAoiMachineCode(),
                log.getTotalModules(),
                log.getNgModules(),
                log.getPerformance(),
                log.getLogFileName(),
                Timestamp.valueOf(log.getCreatedAt())
        );

        log.setLogId(id); // ✅ gán lại cho object luôn
        return id;
    }


    @Override
    public List<PcbPerformanceLog> findAll() {
        String sql = "SELECT * FROM PcbPerformanceLog ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new PcbPerformanceLogRowMapper());
    }

    @Override
    public List<PcbPerformanceLog> findByProductId(int productId) {
        String sql = "SELECT * FROM PcbPerformanceLog WHERE ProductId = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new PcbPerformanceLogRowMapper(), productId);
    }

    @Override
    public List<PcbPerformanceLog> findByWarehouseId(int warehouseId) {
        String sql = "SELECT * FROM PcbPerformanceLog WHERE WarehouseId = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new PcbPerformanceLogRowMapper(), warehouseId);
    }

    @Override
    public boolean existsByCarrierId(String carrierId) {
        String sql = "SELECT COUNT(1) FROM PcbPerformanceLog WHERE CarrierId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, carrierId);
        return count != null && count > 0;
    }

    @Override
    public List<PcbPerformanceLogHistoryDTO> searchLogDTOs(String modelCode, ModelType modelType,
                                                           LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
        SELECT 
            p.ProductCode,
            p.ModelType,
            l.CarrierId,
            l.AoiMachineCode,
            l.TotalModules,
            l.NgModules,
            l.Performance,
            l.LogFileName,
            l.CreatedAt,
            w.Name AS WarehouseName
        FROM PcbPerformanceLog l
        JOIN Products p ON l.ProductId = p.ProductId
        JOIN Warehouses w ON l.WarehouseId = w.WarehouseId
        WHERE 1 = 1
    """);

        newLine(sql, modelCode != null && !modelCode.isBlank(), "AND p.ProductCode LIKE ?");
        newLine(sql, modelType != null, "AND p.ModelType = ?");
        newLine(sql, from != null, "AND l.CreatedAt >= ?");
        newLine(sql, to != null, "AND l.CreatedAt <= ?");
        sql.append(" ORDER BY l.CreatedAt DESC");

        Object[] params = buildParams(modelCode, modelType, from, to);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new PcbPerformanceLogHistoryDTO(
                rs.getString("ProductCode"),
                ModelType.valueOf(rs.getString("ModelType")),
                rs.getString("CarrierId"),
                rs.getString("AoiMachineCode"),
                rs.getInt("TotalModules"),
                rs.getInt("NgModules"),
                rs.getDouble("Performance"),
                rs.getString("LogFileName"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
                rs.getString("WarehouseName")
        ), params);
    }


    private void newLine(StringBuilder sql) {
        sql.append("\n");
    }

    private void newLine(StringBuilder sql, boolean condition, String clause) {
        if (condition) sql.append(clause).append("\n");
    }

    private Object[] buildParams(String modelCode, ModelType modelType, LocalDateTime from, LocalDateTime to) {
        List<Object> params = new java.util.ArrayList<>();
        if (modelCode != null && !modelCode.isBlank()) params.add("%" + modelCode.trim() + "%");
        if (modelType != null) params.add(modelType.name());
        if (from != null) params.add(Timestamp.valueOf(from));
        if (to != null) params.add(Timestamp.valueOf(to));
        return params.toArray();
    }
    @Override
    public boolean existsByFileName(String fileName) {
        String sql = "SELECT COUNT(1) FROM PcbPerformanceLog WHERE LogFileName = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fileName);
        return count != null && count > 0;
    }

    @Override
    public List<PcbPerformanceLogHistoryDTO> getLogsByWarehouseAndDateRange(int warehouseId, LocalDateTime start, LocalDateTime end) {
        String sql = """
        SELECT
            pr.ProductCode AS ModelCode,
            pr.ModelType AS ModelType,
            p.CarrierId,
            p.AoiMachineCode,
            p.TotalModules,
            p.NgModules,
            p.Performance,
            p.LogFileName,
            p.CreatedAt,
            w.Name AS WarehouseName
        FROM PcbPerformanceLog p
        JOIN Warehouses w ON w.WarehouseId = p.WarehouseId
        JOIN Products pr ON pr.ProductId = p.ProductId
        WHERE p.WarehouseId = ?
          AND p.CreatedAt BETWEEN ? AND ?
    """;

        return jdbcTemplate.query(sql, new Object[]{warehouseId, start, end}, (rs, rowNum) -> {
            PcbPerformanceLogHistoryDTO dto = new PcbPerformanceLogHistoryDTO();
            dto.setModelCode(rs.getString("ModelCode"));
            dto.setModelType(ModelType.valueOf(rs.getString("ModelType")));
            dto.setCarrierId(rs.getString("CarrierId"));
            dto.setAoi(rs.getString("AoiMachineCode"));
            dto.setTotalModules(rs.getInt("TotalModules"));
            dto.setNgModules(rs.getInt("NgModules"));
            dto.setPerformance(rs.getDouble("Performance"));
            dto.setLogFileName(rs.getString("LogFileName"));
            dto.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
            dto.setWarehouseName(rs.getString("WarehouseName"));
            return dto;
        });
    }


    public class PcbPerformanceLogHistoryRowMapper implements RowMapper<PcbPerformanceLogHistoryDTO> {
        @Override
        public PcbPerformanceLogHistoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            PcbPerformanceLogHistoryDTO dto = new PcbPerformanceLogHistoryDTO();
            dto.setModelCode(rs.getString("model_code"));
            dto.setModelType(ModelType.valueOf(rs.getString("model_type"))); // Enum
            dto.setCarrierId(rs.getString("carrier_id"));
            dto.setAoi(rs.getString("aoi"));
            dto.setTotalModules(rs.getInt("total_modules"));
            dto.setNgModules(rs.getInt("ng_modules"));
            dto.setPerformance(rs.getDouble("performance"));
            dto.setLogFileName(rs.getString("log_file_name"));
            dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            dto.setWarehouseName(rs.getString("warehouse_name"));
            return dto;
        }
    }

    @Override
    public PcbPerformanceLog findPrevLog(int warehouseId, int productId, LocalDateTime beforeTime) {
        String sql = """
        SELECT TOP 1 *
        FROM PcbPerformanceLog
        WHERE WarehouseId = ?
          AND ProductId = ?
          AND CreatedAt < ?
        ORDER BY CreatedAt DESC
    """;

        List<PcbPerformanceLog> results = jdbcTemplate.query(
                sql,
                new PcbPerformanceLogRowMapper(),
                warehouseId, productId, Timestamp.valueOf(beforeTime)
        );

        return results.isEmpty() ? null : results.get(0);
    }



}
