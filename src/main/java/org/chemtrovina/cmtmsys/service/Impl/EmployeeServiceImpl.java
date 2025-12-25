package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.dto.EmployeeExcelDto;
import org.chemtrovina.cmtmsys.model.Department;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.Position;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.repository.base.DepartmentRepository;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.chemtrovina.cmtmsys.repository.base.PositionRepository;
import org.chemtrovina.cmtmsys.service.base.EmployeeExcelService;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeExcelService employeeExcelService;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            EmployeeExcelService employeeExcelService
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeExcelService = employeeExcelService;
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

    @Override
    public void updateManager(int employeeId, String managerName) {
        employeeRepository.updateManager(employeeId, managerName);
    }

    @Override
    public Employee findByFullName(String fullName) {

        if (fullName == null || fullName.isBlank()) return null;

        return employeeRepository.findByFullName(fullName.trim());
    }


    @Override
    @Transactional
    public void importEmployeeFromExcel(File file) {

        // 1️⃣ Read Excel
        List<EmployeeExcelDto> excelList = employeeExcelService.readEmployeeExcel(file);
        if (excelList.isEmpty()) return;

        // 2️⃣ Load employee hiện có
        Map<String, Employee> employeeMap =
                employeeRepository.findAll().stream()
                        .filter(e -> e.getMSCNID1() != null && !e.getMSCNID1().isBlank())
                        .collect(Collectors.toMap(
                                Employee::getMSCNID1,
                                e -> e
                        ));

        List<Employee> insertList = new ArrayList<>();
        List<Employee> updateList = new ArrayList<>();

        // 3️⃣ Phân loại
        for (EmployeeExcelDto x : excelList) {

            if (x.getMscnId1() == null || x.getMscnId1().isBlank())
                continue;

            if (x.getDepartmentName() == null || x.getDepartmentName().isBlank())
                throw new RuntimeException("DepartmentName không được rỗng: " + x.getMscnId1());

            if (x.getPositionName() == null || x.getPositionName().isBlank())
                throw new RuntimeException("PositionName không được rỗng: " + x.getMscnId1());

            Employee existing = employeeMap.get(x.getMscnId1());

            if (existing != null) {
                mapUpdate(existing, x);
                updateList.add(existing);
            } else {
                insertList.add(mapInsert(x));
            }
        }

        // 4️⃣ DB
        if (!insertList.isEmpty()) employeeRepository.batchInsert(insertList);
        if (!updateList.isEmpty()) employeeRepository.batchUpdate(updateList);
    }


    private void mapUpdate(Employee e, EmployeeExcelDto x) {

        e.setFullName(x.getFullName());
        e.setCompany(x.getCompany());
        e.setGender(x.getGender());
        e.setBirthDate(x.getBirthDate());
        e.setEntryDate(x.getEntryDate());
        e.setPhoneNumber(x.getPhoneNumber());
        e.setJobTitle(x.getJobTitle());
        e.setManager(x.getManager());
        e.setNote(x.getNote());

        // ✅ snapshot text
        e.setDepartmentName(x.getDepartmentName());
        e.setPositionName(x.getPositionName());
    }

    private Employee mapInsert(EmployeeExcelDto x) {

        Employee e = new Employee();
        e.setMSCNID1(x.getMscnId1());
        e.setFullName(x.getFullName());
        e.setCompany(x.getCompany());
        e.setGender(x.getGender());
        e.setBirthDate(x.getBirthDate());
        e.setEntryDate(x.getEntryDate());
        e.setPhoneNumber(x.getPhoneNumber());
        e.setJobTitle(x.getJobTitle());
        e.setManager(x.getManager());
        e.setNote(x.getNote());
        e.setStatus(EmployeeStatus.ACTIVE);

        // ✅ snapshot
        e.setDepartmentName(x.getDepartmentName());
        e.setPositionName(x.getPositionName());

        return e;
    }

    @Override
    public List<EmployeeDto> getWorkingEmployees(LocalDate date) {

        return employeeRepository.findAllActiveByDate(date)
                .stream()
                .map(emp -> {
                    EmployeeDto dto = new EmployeeDto();
                    dto.setEmployeeId(emp.getEmployeeId());
                    dto.setMscnId1(emp.getMSCNID1());
                    dto.setFullName(emp.getFullName());
                    dto.setDepartmentName(emp.getDepartmentName());
                    return dto;
                })
                .toList();
    }


}
