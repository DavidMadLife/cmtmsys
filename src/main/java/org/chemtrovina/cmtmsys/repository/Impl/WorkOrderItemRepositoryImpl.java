package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.WorkOrderItem;
import org.chemtrovina.cmtmsys.repository.RowMapper.WorkOrderItemRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderItemRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class WorkOrderItemRepositoryImpl implements WorkOrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkOrderItemRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(WorkOrderItem item) {
        String sql = "INSERT INTO WorkOrderItems (WorkOrderID, ProductID, Quantity, CreatedDate, UpdatedDate) VALUES (?, ?, ?, ?, ?)";
        var now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.update(sql, item.getWorkOrderId(), item.getProductId(), item.getQuantity(), now, now);
    }

    @Override
    public void update(WorkOrderItem item) {
        String sql = "UPDATE WorkOrderItems SET ProductID = ?, Quantity = ?, UpdatedDate = ? WHERE ItemID = ?";
        jdbcTemplate.update(sql, item.getProductId(), item.getQuantity(), Timestamp.valueOf(LocalDateTime.now()), item.getItemId());
    }

    @Override
    public void delete(int itemId) {
        jdbcTemplate.update("DELETE FROM WorkOrderItems WHERE ItemID = ?", itemId);
    }

    @Override
    public void deleteByWorkOrderId(int workOrderId) {
        jdbcTemplate.update("DELETE FROM WorkOrderItems WHERE WorkOrderID = ?", workOrderId);
    }

    @Override
    public List<WorkOrderItem> findByWorkOrderId(int workOrderId) {
        String sql = "SELECT * FROM WorkOrderItems WHERE WorkOrderID = ?";
        return jdbcTemplate.query(sql, new WorkOrderItemRowMapper(), workOrderId);
    }
}
