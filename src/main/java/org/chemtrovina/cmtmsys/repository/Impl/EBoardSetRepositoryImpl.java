package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.EBoardSet;
import org.chemtrovina.cmtmsys.repository.base.EBoardSetRepository;
import org.chemtrovina.cmtmsys.repository.RowMapper.EBoardSetRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EBoardSetRepositoryImpl implements EBoardSetRepository {

    private final JdbcTemplate jdbcTemplate;

    public EBoardSetRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(EBoardSet set) {
        String sql = """
            INSERT INTO EBoardSets (SetName, Description, CreatedAt, UpdatedAt)
            VALUES (?, ?, GETDATE(), GETDATE())
        """;
        jdbcTemplate.update(sql, set.getSetName(), set.getDescription());
    }

    @Override
    public void update(EBoardSet set) {
        String sql = """
            UPDATE EBoardSets
            SET SetName = ?, Description = ?, UpdatedAt = GETDATE()
            WHERE SetId = ?
        """;
        jdbcTemplate.update(sql, set.getSetName(), set.getDescription(), set.getSetId());
    }

    @Override
    public void delete(int setId) {
        String sql = "DELETE FROM EBoardSets WHERE SetId = ?";
        jdbcTemplate.update(sql, setId);
    }

    @Override
    public EBoardSet findById(int id) {
        String sql = "SELECT * FROM EBoardSets WHERE SetId = ?";
        return jdbcTemplate.query(sql, new EBoardSetRowMapper(), id)
                .stream().findFirst().orElse(null);
    }

    @Override
    public EBoardSet findByName(String name) {
        String sql = "SELECT * FROM EBoardSets WHERE SetName = ?";
        return jdbcTemplate.query(sql, new EBoardSetRowMapper(), name)
                .stream().findFirst().orElse(null);
    }

    @Override
    public List<EBoardSet> findAll() {
        String sql = "SELECT * FROM EBoardSets ORDER BY SetName ASC";
        return jdbcTemplate.query(sql, new EBoardSetRowMapper());
    }
}
