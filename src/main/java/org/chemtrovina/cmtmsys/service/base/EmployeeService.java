package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeService {
    void addEmployee(Employee employee);
    void updateEmployee(Employee employee);
    void deleteEmployeeById(int employeeId);
    Employee getEmployeeById(int employeeId);
    List<Employee> getAllEmployees();
    List<EmployeeDto> getAllEmployeeDtos();
    List<EmployeeDto> filterEmployeeDtos(EmployeeStatus status, LocalDate from, LocalDate to);
}
