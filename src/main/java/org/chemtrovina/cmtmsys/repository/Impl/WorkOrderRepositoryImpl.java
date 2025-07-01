package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.WorkOrder;
import org.chemtrovina.cmtmsys.repository.RowMapper.WorkOrderRowMapper;
import org.chemtrovina.cmtmsys.repository.base.WorkOrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public WorkOrderRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(WorkOrder wo) {
        String sql = "INSERT INTO WorkOrders (WorkOrderCode, Description, CreatedDate, UpdatedDate) VALUES (?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, wo.getWorkOrderCode(), wo.getDescription(), Timestamp.valueOf(now), Timestamp.valueOf(now));
    }

    @Override
    public void update(WorkOrder wo) {
        String sql = "UPDATE WorkOrders SET Description = ?, UpdatedDate = ? WHERE WorkOrderID = ?";
        jdbcTemplate.update(sql, wo.getDescription(), Timestamp.valueOf(LocalDateTime.now()), wo.getWorkOrderId());
    }

    @Override
    public void delete(int workOrderId) {
        jdbcTemplate.update("DELETE FROM WorkOrders WHERE WorkOrderID = ?", workOrderId);
    }

    @Override
    public WorkOrder findById(int id) {
        String sql = "SELECT * FROM WorkOrders WHERE WorkOrderID = ?";
        var list = jdbcTemplate.query(sql, new WorkOrderRowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public WorkOrder findByCode(String workOrderCode) {
        String sql = "SELECT * FROM WorkOrders WHERE WorkOrderCode = ?";
        var list = jdbcTemplate.query(sql, new WorkOrderRowMapper(), workOrderCode);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<WorkOrder> findAll() {
        return jdbcTemplate.query("SELECT * FROM WorkOrders ORDER BY WorkOrderID DESC", new WorkOrderRowMapper());
    }
    @Override
    public String findMaxCodeByDate(LocalDate date) {
        String prefix = "WO" + date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String sql = """
        SELECT MAX(WorkOrderCode)
        FROM WorkOrders
        WHERE WorkOrderCode LIKE ?
    """;
        return jdbcTemplate.queryForObject(sql, String.class, prefix + "-%");
    }

    @Override
    public int findIdByCode(String workOrderCode) {
        String sql = "SELECT WorkOrderId FROM WorkOrders WHERE WorkOrderCode = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, workOrderCode);
    }




}
