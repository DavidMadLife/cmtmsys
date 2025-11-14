package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.MaterialConsumeDetailLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaterialConsumeDetailLogRowMapper implements RowMapper<MaterialConsumeDetailLog> {

    @Override
    public MaterialConsumeDetailLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        MaterialConsumeDetailLog log = new MaterialConsumeDetailLog();
        log.setLogId(rs.getInt("LogID"));
        log.setPlanItemId(rs.getInt("PlanItemID"));
        log.setRunDate(rs.getObject("RunDate", LocalDate.class));
        log.setMaterialId(rs.getInt("MaterialID"));
        log.setConsumedQty(rs.getInt("ConsumedQty"));
        log.setSourceLogId(rs.getObject("SourceLogId") != null ? rs.getInt("SourceLogId") : null);
        log.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
        return log;
    }
}
