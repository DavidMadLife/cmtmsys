package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.Department;
import org.chemtrovina.cmtmsys.repository.RowMapper.DepartmentRowMapper;
import org.chemtrovina.cmtmsys.repository.base.DepartmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Department department) {
        String sql = "INSERT INTO Department (DepartmentName) VALUES (?)";
        jdbcTemplate.update(sql, department.getDepartmentName());
    }

    @Override
    public void update(Department department) {
        String sql = "UPDATE Department SET DepartmentName = ? WHERE DepartmentID = ?";
        jdbcTemplate.update(sql, department.getDepartmentName(), department.getDepartmentID());
    }

    @Override
    public void deleteById(int departmentId) {
        String sql = "DELETE FROM Department WHERE DepartmentID = ?";
        jdbcTemplate.update(sql, departmentId);
    }

    @Override
    public Department findById(int departmentId) {
        String sql = "SELECT * FROM Department WHERE DepartmentID = ?";
        List<Department> result = jdbcTemplate.query(sql, new DepartmentRowMapper(), departmentId);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Department> findAll() {
        String sql = "SELECT * FROM Department";
        return jdbcTemplate.query(sql, new DepartmentRowMapper());
    }
}
