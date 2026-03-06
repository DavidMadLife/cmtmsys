package org.chemtrovina.cmtmsys.repository.impl;

import org.chemtrovina.cmtmsys.model.MaterialMakerMapping;
import org.chemtrovina.cmtmsys.repository.base.MaterialMakerMappingRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MaterialMakerMappingRepositoryImpl implements MaterialMakerMappingRepository {

    private final JdbcTemplate jdbc;
    private final BeanPropertyRowMapper<MaterialMakerMapping> mapper =
            new BeanPropertyRowMapper<>(MaterialMakerMapping.class);

    public MaterialMakerMappingRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<MaterialMakerMapping> findAll() {
        String sql = """
            SELECT
                Id,
                RollCode,
                Spec,
                MakerPN,
                Maker,
                CreatedAt,
                UpdatedAt
            FROM MaterialMakerMapping
            ORDER BY UpdatedAt DESC, Id DESC
        """;
        return jdbc.query(sql, mapper);
    }

    @Override
    public MaterialMakerMapping findById(int id) {
        String sql = """
            SELECT
                Id,
                RollCode,
                Spec,
                MakerPN,
                Maker,
                CreatedAt,
                UpdatedAt
            FROM MaterialMakerMapping
            WHERE Id = ?
        """;
        List<MaterialMakerMapping> list = jdbc.query(sql, mapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public MaterialMakerMapping findByRollCode(String rollCode) {
        String sql = """
            SELECT TOP 1
                Id,
                RollCode,
                Spec,
                MakerPN,
                Maker,
                CreatedAt,
                UpdatedAt
            FROM MaterialMakerMapping
            WHERE RollCode = ?
            ORDER BY UpdatedAt DESC, Id DESC
        """;
        List<MaterialMakerMapping> list = jdbc.query(sql, mapper, rollCode);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public int insert(MaterialMakerMapping m) {
        String sql = """
            INSERT INTO MaterialMakerMapping (
                RollCode,
                Spec,
                MakerPN,
                Maker,
                CreatedAt,
                UpdatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        return jdbc.update(sql,
                m.getRollCode(),
                m.getSpec(),
                m.getMakerPN(),
                m.getMaker(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }

    @Override
    public int update(MaterialMakerMapping m) {
        String sql = """
            UPDATE MaterialMakerMapping
            SET
                RollCode = ?,
                Spec = ?,
                MakerPN = ?,
                Maker = ?,
                CreatedAt = ?,
                UpdatedAt = ?
            WHERE Id = ?
        """;
        return jdbc.update(sql,
                m.getRollCode(),
                m.getSpec(),
                m.getMakerPN(),
                m.getMaker(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                m.getId()
        );
    }

    @Override
    public int delete(int id) {
        String sql = "DELETE FROM MaterialMakerMapping WHERE Id = ?";
        return jdbc.update(sql, id);
    }

    @Override
    public int deleteByRollCode(String rollCode) {
        String sql = "DELETE FROM MaterialMakerMapping WHERE RollCode = ?";
        return jdbc.update(sql, rollCode);
    }

    @Override
    public int truncate() {
        // SQL Server: TRUNCATE TABLE nhanh, nhưng cần quyền.
        // Nếu bạn không muốn TRUNCATE, đổi sang: DELETE FROM ...
        String sql = "TRUNCATE TABLE MaterialMakerMapping";
        return jdbc.update(sql);
    }
}
