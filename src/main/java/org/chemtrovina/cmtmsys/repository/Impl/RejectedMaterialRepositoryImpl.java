package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.RejectedMaterialDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.chemtrovina.cmtmsys.repository.RowMapper.RejectedMaterialRowMapper;
import org.chemtrovina.cmtmsys.repository.base.RejectedMaterialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RejectedMaterialRepositoryImpl implements RejectedMaterialRepository {

    private final JdbcTemplate jdbcTemplate;

    public RejectedMaterialRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(RejectedMaterial rejectedMaterial) {
        String sql = """
            INSERT INTO RejectedMaterials 
            (workOrderId, warehouseId, sapCode, quantity, note, createdDate)
            VALUES (?, ?, ?, ?, ?, GETDATE())
        """;
        jdbcTemplate.update(sql,
                rejectedMaterial.getWorkOrderId(),
                rejectedMaterial.getWarehouseId(),
                rejectedMaterial.getSapCode(),
                rejectedMaterial.getQuantity(),
                rejectedMaterial.getNote()
        );
    }

    @Override
    public List<RejectedMaterial> findByWorkOrderId(int workOrderId) {
        String sql = "SELECT * FROM RejectedMaterials WHERE workOrderId = ?";
        return jdbcTemplate.query(sql, new RejectedMaterialRowMapper(), workOrderId);
    }

    @Override
    public List<RejectedMaterial> findByWarehouseId(int warehouseId) {
        String sql = "SELECT * FROM RejectedMaterials WHERE warehouseId = ?";
        return jdbcTemplate.query(sql, new RejectedMaterialRowMapper(), warehouseId);
    }

    @Override
    public List<RejectedMaterial> findByWorkOrderIdAndSapCode(int workOrderId, String sapCode) {
        String sql = "SELECT * FROM RejectedMaterials WHERE workOrderId = ? AND sapCode = ?";
        return jdbcTemplate.query(sql, new RejectedMaterialRowMapper(), workOrderId, sapCode);
    }

    @Override
    public RejectedMaterial findSingleByWorkOrderIdAndSapCode(int workOrderId, String sapCode) {
        String sql = "SELECT * FROM RejectedMaterials WHERE workOrderId = ? AND sapCode = ?";
        List<RejectedMaterial> list = jdbcTemplate.query(sql, new RejectedMaterialRowMapper(), workOrderId, sapCode);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<RejectedMaterialDto> findAllDto() {
        String sql = """
    SELECT rm.Id, wo.WorkOrderCode, rm.sapCode, rm.quantity,
           wh.Name AS warehouseName, rm.note, rm.createdDate
    FROM RejectedMaterials rm
    JOIN WorkOrders wo ON rm.workOrderId = wo.workOrderId
    JOIN Warehouses wh ON rm.warehouseId = wh.warehouseId
    ORDER BY rm.createdDate DESC
""";


        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RejectedMaterialDto dto = new RejectedMaterialDto();
            dto.setId(rs.getInt("Id")); // ✅ sửa lại đúng tên cột thật trong DB
            dto.setWorkOrderCode(rs.getString("WorkOrderCode"));
            dto.setSapCode(rs.getString("sapCode"));
            dto.setQuantity(rs.getInt("quantity"));
            dto.setWarehouseName(rs.getString("warehouseName"));
            dto.setNote(rs.getString("note"));
            dto.setCreatedDate(rs.getTimestamp("createdDate").toLocalDateTime());
            return dto;
        });

    }


    @Override
    public void updateNoteById(int id, String note) {
        String sql = "UPDATE RejectedMaterials SET note = ? WHERE id = ?";
        jdbcTemplate.update(sql, note, id);
    }


    @Override
    public void update(RejectedMaterial rejectedMaterial) {
        String sql = """
        UPDATE RejectedMaterials
        SET quantity = ?, note = ?, createdDate = ?
        WHERE workOrderId = ? AND sapCode = ?
    """;
        jdbcTemplate.update(sql,
                rejectedMaterial.getQuantity(),
                rejectedMaterial.getNote(),
                rejectedMaterial.getCreatedDate(),
                rejectedMaterial.getWorkOrderId(),
                rejectedMaterial.getSapCode()
        );
    }

}
