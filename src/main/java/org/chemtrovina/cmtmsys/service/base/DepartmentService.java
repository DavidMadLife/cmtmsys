package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Department;

import java.util.List;

public interface DepartmentService {
    void addDepartment(Department department);
    void updateDepartment(Department department);
    void deleteDepartmentById(int departmentId);
    Department getDepartmentById(int departmentId);
    List<Department> getAllDepartments();
}
