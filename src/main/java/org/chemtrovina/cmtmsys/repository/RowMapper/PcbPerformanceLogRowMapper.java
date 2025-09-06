package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PcbPerformanceLogRowMapper implements RowMapper<PcbPerformanceLog> {

    @Override
    public PcbPerformanceLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        PcbPerformanceLog log = new PcbPerformanceLog();

        log.setLogId(rs.getInt("LogId"));
        log.setProductId(rs.getInt("ProductId"));
        log.setWarehouseId(rs.getInt("WarehouseId"));
        log.setCarrierId(rs.getString("CarrierId"));
        log.setAoiMachineCode(rs.getString("AoiMachineCode"));
        log.setTotalModules(rs.getInt("TotalModules"));
        log.setNgModules(rs.getInt("NgModules"));
        log.setPerformance(rs.getDouble("Performance"));
        log.setLogFileName(rs.getString("LogFileName"));

        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }

        return log;
    }
}
