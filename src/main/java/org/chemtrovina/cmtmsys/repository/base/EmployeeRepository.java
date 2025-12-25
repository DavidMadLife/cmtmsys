package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.dto.EmployeeExcelDto;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository {

    void add(Employee employee);
    void update(Employee employee);
    void deleteById(int employeeId);
    Employee findById(int employeeId);
    List<Employee> findAll();
    List<Employee> findAllActive();
    List<Employee> findAllActiveByDate(LocalDate date);

    List<EmployeeDto> findAllEmployeeDtos();
    List<EmployeeDto> findFilteredEmployeeDtos(EmployeeStatus status, LocalDate entryDateFrom, LocalDate entryDateTo);

    Employee findByMscnId1(String mscnId1);
    Employee findByMscnId2(String mscnId2);

    void updateManager(int employeeId, String managerName);

    List<Employee> findByIds(List<Integer> ids);
    Employee findByCardId(String cardId);

    void batchInsert(List<Employee> employees);
    void batchUpdate(List<Employee> employees);

    Employee findByFullName(String fullName);


}
