package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Position;
import org.chemtrovina.cmtmsys.repository.RowMapper.PositionRowMapper;
import org.chemtrovina.cmtmsys.repository.base.PositionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PositionRepositoryImpl implements PositionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PositionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Position position) {
        String sql = "INSERT INTO Position (PositionName) VALUES (?)";
        jdbcTemplate.update(sql, position.getPositionName());
    }

    @Override
    public void update(Position position) {
        String sql = "UPDATE Position SET PositionName = ? WHERE PositionId = ?";
        jdbcTemplate.update(sql, position.getPositionName(), position.getPositionId());
    }

    @Override
    public void deleteById(int positionId) {
        String sql = "DELETE FROM Position WHERE PositionId = ?";
        jdbcTemplate.update(sql, positionId);
    }

    @Override
    public Position findById(int positionId) {
        String sql = "SELECT * FROM Position WHERE PositionId = ?";
        List<Position> result = jdbcTemplate.query(sql, new PositionRowMapper(), positionId);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Position> findAll() {
        String sql = "SELECT * FROM Position";
        return jdbcTemplate.query(sql, new PositionRowMapper());
    }
}
