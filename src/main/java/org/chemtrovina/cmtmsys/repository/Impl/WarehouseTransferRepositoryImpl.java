package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.chemtrovina.cmtmsys.repository.RowMapper.WarehouseTransferRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class WarehouseTransferRepositoryImpl implements WarehouseTransferRepository {

    private final JdbcTemplate jdbcTemplate;

    public WarehouseTransferRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(WarehouseTransfer transfer) {
        String sql = "INSERT INTO WarehouseTransfers (WorkOrderID, FromWarehouseID, ToWarehouseID, TransferDate, Note, EmployeeID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                transfer.getWorkOrderId(),
                transfer.getFromWarehouseId(),
                transfer.getToWarehouseId(),
                transfer.getTransferDate(),
                transfer.getNote(),
                transfer.getEmployeeId()
        );
    }

    @Override
    public List<WarehouseTransfer> getAll() {
        String sql = "SELECT * FROM WarehouseTransfers";
        return jdbcTemplate.query(sql, new WarehouseTransferRowMapper());
    }

    @Override
    public WarehouseTransfer findById(int id) {
        String sql = "SELECT * FROM WarehouseTransfers WHERE TransferID = ?";
        return jdbcTemplate.queryForObject(sql, new WarehouseTransferRowMapper(), id);
    }

    public Map<String,Integer> getScannedQuantitiesByWO(int workOrderId) {
        String sql = """
      SELECT d.SapCode, SUM(d.Quantity) AS Scanned
      FROM WarehouseTransferDetails d
      JOIN WarehouseTransfers t ON d.TransferID = t.TransferID
      WHERE t.WorkOrderID = ?
      GROUP BY d.SapCode
    """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String,Integer> m = new HashMap<>();
            while (rs.next()) m.put(rs.getString("SapCode"), rs.getInt("Scanned"));
            return m;
        }, workOrderId);
    }

    @Override
    public void deleteByWorkOrderId(int workOrderId) {
        String sql = "DELETE FROM WarehouseTransfers WHERE WorkOrderID = ?";
        jdbcTemplate.update(sql, workOrderId);
    }

    @Override
    public Map<String, Integer> getActualReturnedByWorkOrderId(int workOrderId) {
        String sql = """
        SELECT SAPCode, SUM(ActualReturned) as totalReturned
        FROM WarehouseTransferDetails d
        JOIN WarehouseTransfers t ON d.TransferID = t.TransferID
        WHERE t.WorkOrderID = ?
        GROUP BY SAPCode
    """;

        return jdbcTemplate.query(sql, new Object[]{workOrderId}, rs -> {
            Map<String, Integer> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("SAPCode"), rs.getInt("totalReturned"));
            }
            return map;
        });
    }

    @Override
    public int getFromWarehouseIdByTransferId(int transferId) {
        String sql = "SELECT FromWarehouseId FROM WarehouseTransfers WHERE TransferId = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, transferId);
    }


}

