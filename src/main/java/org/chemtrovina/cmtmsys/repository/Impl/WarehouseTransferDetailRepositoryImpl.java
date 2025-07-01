package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.chemtrovina.cmtmsys.repository.RowMapper.WarehouseTransferDetailRowMapper;
import org.chemtrovina.cmtmsys.repository.RowMapper.WarehouseTransferRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferDetailRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

public class WarehouseTransferDetailRepositoryImpl implements WarehouseTransferDetailRepository {

    private final JdbcTemplate jdbcTemplate;

    public WarehouseTransferDetailRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(WarehouseTransferDetail detail) {
        String sql = "INSERT INTO WarehouseTransferDetails (TransferID, RollCode, SAPCode, Quantity, CreatedAt) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                detail.getTransferId(),
                detail.getRollCode(),
                detail.getSapCode(),
                detail.getQuantity(),
                detail.getCreatedAt()
        );
    }

    @Override
    public List<WarehouseTransferDetail> findByTransferId(int transferId) {
        String sql = "SELECT * FROM WarehouseTransferDetails WHERE TransferID = ?";
        return jdbcTemplate.query(sql, new WarehouseTransferDetailRowMapper(), transferId);
    }

    @Override
    public List<WarehouseTransferDetail> findByWorkOrderId(int workOrderId) {
        String sql = "SELECT d.* FROM WarehouseTransferDetails d " +
                "JOIN WarehouseTransfers t ON d.TransferID = t.TransferID " +
                "WHERE t.WorkOrderID = ?";
        return jdbcTemplate.query(sql, new WarehouseTransferDetailRowMapper(), workOrderId);
    }

    @Override
    public boolean existsByTransferIdAndRollCode(int transferId, String rollCode) {
        String sql = "SELECT COUNT(*) FROM WarehouseTransferDetails WHERE TransferID = ? AND RollCode = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, transferId, rollCode);
        return count != null && count > 0;
    }

    @Override
    public Optional<WarehouseTransfer> findByFields(int fromWarehouseId, int toWarehouseId, int workOrderId, String employeeId) {
        String sql = "SELECT * FROM WarehouseTransfers WHERE FromWarehouseID = ? AND ToWarehouseID = ? AND WorkOrderID = ? AND EmployeeID = ?";
        List<WarehouseTransfer> results = jdbcTemplate.query(sql, new WarehouseTransferRowMapper(),
                fromWarehouseId, toWarehouseId, workOrderId, employeeId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void deleteByWorkOrderId(int workOrderId) {
        String sql = """
        DELETE FROM WarehouseTransferDetails
        WHERE TransferID IN (
            SELECT TransferID FROM WarehouseTransfers WHERE WorkOrderID = ?
        )
    """;
        jdbcTemplate.update(sql, workOrderId);
    }




}
