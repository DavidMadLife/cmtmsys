package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ProductionPlanItem;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductionPlanItemRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanItemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductionPlanItemRepositoryImpl implements ProductionPlanItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductionPlanItemRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ProductionPlanItem item) {
        String sql = "INSERT INTO ProductionPlanItems (PlanID, ProductID, PlannedQuantity) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, item.getPlanID(), item.getProductID(), item.getPlannedQuantity());
    }

    @Override
    public void update(ProductionPlanItem item) {
        String sql = "UPDATE ProductionPlanItems SET ProductID = ?, PlannedQuantity = ? WHERE PlanItemID = ?";
        jdbcTemplate.update(sql, item.getProductID(), item.getPlannedQuantity(), item.getPlanItemID());
    }

    @Override
    public void deleteById(int itemId) {
        jdbcTemplate.update("DELETE FROM ProductionPlanItems WHERE PlanItemID = ?", itemId);
    }

    @Override
    public List<ProductionPlanItem> findByPlanId(int planId) {
        String sql = "SELECT * FROM ProductionPlanItems WHERE PlanID = ?";
        return jdbcTemplate.query(sql, new ProductionPlanItemRowMapper(), planId);
    }
}
