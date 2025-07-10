package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.repository.base.GenericRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


public class GenericRepositoryImpl<T> implements GenericRepository<T> {

    protected final JdbcTemplate jdbcTemplate;
    protected final RowMapper<T> rowMapper;
    protected final String tableName;

    public GenericRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<T> rowMapper, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.tableName = tableName;
    }

    @Override
    public T findById(int id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, rowMapper);
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void add(T entity) {

    }

    @Override
    public void update(T entity) {

    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
