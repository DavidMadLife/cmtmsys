package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository {

    void add(Employee employee);
    void update(Employee employee);
    void deleteById(int employeeId);
    Employee findById(int employeeId);
    List<Employee> findAll();

    List<EmployeeDto> findAllEmployeeDtos();
    List<EmployeeDto> findFilteredEmployeeDtos(EmployeeStatus status, LocalDate entryDateFrom, LocalDate entryDateTo);

    Employee findByMscnId1(String mscnId1);
    Employee findByMscnId2(String mscnId2);
}
