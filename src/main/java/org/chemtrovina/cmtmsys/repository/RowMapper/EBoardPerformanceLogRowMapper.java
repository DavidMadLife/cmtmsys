package org.chemtrovina.cmtmsys.repository.RowMapper;


import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class EBoardPerformanceLogRowMapper implements RowMapper<EBoardPerformanceLog> {
    @Override
    public EBoardPerformanceLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        EBoardPerformanceLog log = new EBoardPerformanceLog();
        log.setLogId(rs.getInt("LogId"));
        log.seteBoardProductId(rs.getInt("EBoardProductId"));
        log.setSetId(rs.getInt("SetId"));
        log.setWarehouseId(rs.getInt("WarehouseId"));
        log.setCircuitType(rs.getString("CircuitType"));
        log.setModelType(rs.getString("ModelType"));
        log.setCarrierId(rs.getString("CarrierId"));
        log.setAoiMachineCode(rs.getString("AoiMachineCode"));
        log.setTotalModules(rs.getInt("TotalModules"));
        log.setNgModules(rs.getInt("NgModules"));
        log.setPerformance(rs.getDouble("Performance"));
        log.setLogFileName(rs.getString("LogFileName"));

        var created = rs.getTimestamp("CreatedAt");
        var updated = rs.getTimestamp("UpdatedAt");
        log.setCreatedAt(created != null ? created.toLocalDateTime() : LocalDateTime.now());
        log.setUpdatedAt(updated != null ? updated.toLocalDateTime() : LocalDateTime.now());

        return log;
    }
}
