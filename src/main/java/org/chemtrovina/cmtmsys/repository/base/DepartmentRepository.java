package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Department;

import java.util.List;

public interface DepartmentRepository {
    void add(Department department);
    void update(Department department);
    void deleteById(int departmentId);
    Department findById(int departmentId);
    List<Department> findAll();
    String getName(Integer departmentId);
}
