package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.ProductionGapLog;
import org.chemtrovina.cmtmsys.repository.RowMapper.ProductionGapLogRowMapper;
import org.chemtrovina.cmtmsys.repository.base.ProductionGapLogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductionGapLogRepositoryImpl implements ProductionGapLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductionGapLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(ProductionGapLog gapLog) {
        String sql = "INSERT INTO ProductionGapLog (ProductId, WarehouseId, PrevLogId, CurrLogId, PidDistanceSec, Status, Reason) VALUES (?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, gapLog.getProductId(),
                gapLog.getWarehouseId(),
                gapLog.getPrevLogId(),
                gapLog.getCurrLogId(),
                gapLog.getPidDistanceSec(),
                gapLog.getStatus(),
                gapLog.getReason());
    }

    @Override
    public void deleteById(long gapId) {
        jdbcTemplate.update("DELETE FROM ProductionGapLog WHERE GapId=?", gapId);
    }

    @Override
    public ProductionGapLog findById(long gapId) {
        String sql = "SELECT * FROM ProductionGapLog WHERE GapId=?";
        List<ProductionGapLog> list = jdbcTemplate.query(sql, new ProductionGapLogRowMapper(), gapId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ProductionGapLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM ProductionGapLog ORDER BY CreatedAt DESC", new ProductionGapLogRowMapper());
    }

    @Override
    public List<ProductionGapLog> findByShift(int warehouseId, String start, String end) {
        String sql = "SELECT * FROM ProductionGapLog WHERE WarehouseId=? AND CreatedAt BETWEEN ? AND ? ORDER BY CreatedAt";
        return jdbcTemplate.query(sql, new ProductionGapLogRowMapper(), warehouseId, start, end);
    }
}
