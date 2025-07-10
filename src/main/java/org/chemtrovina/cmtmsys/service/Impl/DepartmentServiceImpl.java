package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Department;
import org.chemtrovina.cmtmsys.repository.base.DepartmentRepository;
import org.chemtrovina.cmtmsys.service.base.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void addDepartment(Department department) {
        departmentRepository.add(department);
    }

    @Override
    public void updateDepartment(Department department) {
        departmentRepository.update(department);
    }

    @Override
    public void deleteDepartmentById(int departmentId) {
        departmentRepository.deleteById(departmentId);
    }

    @Override
    public Department getDepartmentById(int departmentId) {
        return departmentRepository.findById(departmentId);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
