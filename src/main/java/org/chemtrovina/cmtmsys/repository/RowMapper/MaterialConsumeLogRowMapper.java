package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.MaterialConsumeLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaterialConsumeLogRowMapper implements RowMapper<MaterialConsumeLog> {
    @Override
    public MaterialConsumeLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MaterialConsumeLog(
                rs.getInt("ConsumeID"),
                rs.getInt("PlanItemID"),
                rs.getDate("RunDate").toLocalDate(),
                rs.getInt("ConsumedQty"),
                rs.getTimestamp("CreatedAt").toLocalDateTime()
        );
    }
}
