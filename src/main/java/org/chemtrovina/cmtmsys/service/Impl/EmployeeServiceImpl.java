package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void addEmployee(Employee employee) {
        employeeRepository.add(employee);
    }

    @Override
    public void updateEmployee(Employee employee) {
        employeeRepository.update(employee);
    }

    @Override
    public void deleteEmployeeById(int employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    @Override
    public Employee getEmployeeById(int employeeId) {
        return employeeRepository.findById(employeeId);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public List<EmployeeDto> getAllEmployeeDtos() {
        return employeeRepository.findAllEmployeeDtos();
    }

    @Override
    public List<EmployeeDto> filterEmployeeDtos(EmployeeStatus status, LocalDate from, LocalDate to) {
        return employeeRepository.findFilteredEmployeeDtos(status, from, to);
    }

    @Override
    public Employee getByMscnId1(String mscnId1) {
        return employeeRepository.findByMscnId1(mscnId1);
    }

    @Override
    public Employee getByMscnId2(String mscnId2) {
        return employeeRepository.findByMscnId2(mscnId2);
    }

}
