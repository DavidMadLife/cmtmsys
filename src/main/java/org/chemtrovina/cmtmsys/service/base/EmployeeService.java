package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.dto.EmployeeExcelDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.io.File;
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

    Employee getByMscnId1(String mscnId1);
    Employee getByMscnId2(String mscnId2);

    void updateManager(int employeeId, String managerName);
    void importEmployeeFromExcel(File file, LocalDate importDate);

    List<EmployeeDto> getWorkingEmployees(LocalDate date);

    Employee findByFullName(String fullName);

}
