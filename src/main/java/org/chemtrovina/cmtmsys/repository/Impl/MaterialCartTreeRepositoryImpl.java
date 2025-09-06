package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.MaterialCartTree;
import org.chemtrovina.cmtmsys.repository.base.MaterialCartTreeRepository;
import org.chemtrovina.cmtmsys.repository.RowMapper.MaterialCartTreeRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MaterialCartTreeRepositoryImpl implements MaterialCartTreeRepository {

    private final JdbcTemplate jdbcTemplate;

    public MaterialCartTreeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(MaterialCartTree tree) {
        String sql = "INSERT INTO MaterialCartTrees (CartID, TreeCode, CreatedAt, LevelNote) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, tree.getCartId(), tree.getTreeCode(), tree.getCreatedAt(), tree.getLevelNote());
    }


    @Override
    public List<MaterialCartTree> findByCartId(int cartId) {
        String sql = "SELECT * FROM MaterialCartTrees WHERE CartID = ?";
        return jdbcTemplate.query(sql, new MaterialCartTreeRowMapper(), cartId);
    }

    @Override
    public MaterialCartTree findByCode(String treeCode) {
        String sql = "SELECT * FROM MaterialCartTrees WHERE TreeCode = ?";
        List<MaterialCartTree> list = jdbcTemplate.query(sql, new MaterialCartTreeRowMapper(), treeCode);
        return list.isEmpty() ? null : list.get(0);  // chỉ lấy dòng đầu
    }


    @Override
    public MaterialCartTree getById(int treeId) {
        String sql = "SELECT * FROM MaterialCartTrees WHERE TreeID = ?";
        List<MaterialCartTree> list = jdbcTemplate.query(sql, new MaterialCartTreeRowMapper(), treeId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void deleteById(int treeId) {
        // 1. Gỡ các cuộn ra khỏi cây
        String updateSql = "UPDATE Materials SET TreeID = NULL WHERE TreeID = ?";
        jdbcTemplate.update(updateSql, treeId);

        // 2. Xoá cây
        String deleteSql = "DELETE FROM MaterialCartTrees WHERE TreeID = ?";
        jdbcTemplate.update(deleteSql, treeId);
    }

    @Override
    public void update(MaterialCartTree tree) {
        String sql = "UPDATE MaterialCartTrees SET TreeCode = ?, CreatedAt = ?, LevelNote = ? WHERE TreeID = ?";
        jdbcTemplate.update(sql, tree.getTreeCode(), tree.getCreatedAt(), tree.getLevelNote(), tree.getTreeId());
    }

    @Override
    public List<MaterialCartTree> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM MaterialCartTrees WHERE TreeID IN (" + placeholders + ")";
        return jdbcTemplate.query(sql, new MaterialCartTreeRowMapper(), ids.toArray());
    }

    @Override
    public MaterialCartTree getTreeByCartIdAndTreeCode(int cartId, String treeCode) {
        String sql = "SELECT * FROM MaterialCartTrees WHERE CartID = ? AND TreeCode = ?";
        List<MaterialCartTree> list = jdbcTemplate.query(sql, new MaterialCartTreeRowMapper(), cartId, treeCode);
        return list.isEmpty() ? null : list.get(0);
    }







}
