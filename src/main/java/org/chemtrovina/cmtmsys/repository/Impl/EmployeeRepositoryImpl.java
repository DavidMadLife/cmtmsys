package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.repository.RowMapper.EmployeeRowMapper;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class EmployeeRepositoryImpl extends GenericRepositoryImpl<Employee> implements EmployeeRepository {

    public EmployeeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new EmployeeRowMapper(), "Employee");
    }

    @Override
    public void add(Employee employee) {
        String sql = "INSERT INTO Employee (MSCNID1, MSCNID2, FullName, Company, Gender, DateOfBirth, EntryDate, ExitDate, Address, PhoneNumber, DepartmentId, PositionId, ShiftId, ManagerId, JobTitle, Note, Status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                employee.getMSCNID1(),
                employee.getMSCNID2(),
                employee.getFullName(),
                employee.getCompany(),
                employee.getGender(),
                employee.getDateOfBirth(),
                employee.getEntryDate(),
                employee.getExitDate(),
                employee.getAddress(),
                employee.getPhoneNumber(),
                employee.getDepartmentId(),
                employee.getPositionId(),
                employee.getManagerId(),
                employee.getJobTitle(),
                employee.getNote(),
                employee.getStatus()
        );
    }

    @Override
    public void update(Employee employee) {
        String sql = "UPDATE Employee SET MSCNID1 = ?, MSCNID2 = ?, FullName = ?, Company = ?, Gender = ?, DateOfBirth = ?, EntryDate = ?, ExitDate = ?, Address = ?, PhoneNumber = ?, DepartmentId = ?, PositionId = ?, ShiftId = ?, ManagerId = ?, JobTitle = ?, Note = ?, Status = ? " +
                "WHERE EmployeeId = ?";
        jdbcTemplate.update(sql,
                employee.getMSCNID1(),
                employee.getMSCNID2(),
                employee.getFullName(),
                employee.getCompany(),
                employee.getGender(),
                employee.getDateOfBirth(),
                employee.getEntryDate(),
                employee.getExitDate(),
                employee.getAddress(),
                employee.getPhoneNumber(),
                employee.getDepartmentId(),
                employee.getPositionId(),
                employee.getManagerId(),
                employee.getJobTitle(),
                employee.getNote(),
                employee.getStatus(),
                employee.getEmployeeId()
        );
    }

    @Override
    public void deleteById(int employeeId) {
        String sql = "DELETE FROM Employee WHERE EmployeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }

    @Override
    public Employee findById(int employeeId) {
        String sql = "SELECT * FROM Employee WHERE EmployeeId = ?";
        List<Employee> result = jdbcTemplate.query(sql, new EmployeeRowMapper(), employeeId);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM Employee";
        return jdbcTemplate.query(sql, new EmployeeRowMapper());
    }

    @Override
    public List<EmployeeDto> findAllEmployeeDtos() {
        String sql = """
            SELECT 
                e.EmployeeID,
                e.MSCNID1,
                e.MSCNID2,
                e.FullName,
                e.Company,
                e.Gender,
                e.BirthDate,
                e.EntryDate,
                e.ExitDate,
                e.Address,
                e.PhoneNumber,
                e.JobTitle,
                e.Note,
                e.Status,
                d.DepartmentName,
                p.PositionName,
                m.FullName AS ManagerName
            FROM Employee e
            LEFT JOIN Department d ON e.DepartmentID = d.DepartmentID
            LEFT JOIN Position p ON e.PositionID = p.PositionID
            LEFT JOIN Employee m ON e.ManagerID = m.EmployeeID
            ORDER BY e.EmployeeID
        """;

        return jdbcTemplate.query(sql, employeeDtoMapper);
    }

    public List<EmployeeDto> findFilteredEmployeeDtos(EmployeeStatus status, LocalDate entryDateFrom, LocalDate entryDateTo) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                e.EmployeeID,
                e.MSCNID1,
                e.MSCNID2,
                e.FullName,
                e.Company,
                e.Gender,
                e.BirthDate,
                e.EntryDate,
                e.ExitDate,
                e.Address,
                e.PhoneNumber,
                e.JobTitle,
                e.Note,
                e.Status,
                d.DepartmentName,
                p.PositionName,
                m.FullName AS ManagerName
            FROM Employee e
            LEFT JOIN Department d ON e.DepartmentID = d.DepartmentID
            LEFT JOIN Position p ON e.PositionID = p.PositionID
            LEFT JOIN Employee m ON e.ManagerID = m.EmployeeID
            WHERE 1=1
        """);

        List<Object> params = new java.util.ArrayList<>();

        if (status != null) {
            sql.append(" AND e.Status = ?");
            params.add(status.getCode());
        }
        if (entryDateFrom != null) {
            sql.append(" AND e.EntryDate >= ?");
            params.add(entryDateFrom);
        }
        if (entryDateTo != null) {
            sql.append(" AND e.EntryDate <= ?");
            params.add(entryDateTo);
        }

        sql.append(" ORDER BY e.EmployeeID");

        return jdbcTemplate.query(sql.toString(), params.toArray(), employeeDtoMapper);
    }

    ////////////////////////////////////Mapper////////////////////////////////////////////
    private final RowMapper<EmployeeDto> employeeDtoMapper = (rs, rowNum) -> {
        EmployeeDto dto = new EmployeeDto();
        dto.setNo(rowNum + 1);
        dto.setMscnId1(rs.getString("MSCNID1"));
        dto.setMscnId2(rs.getString("MSCNID2"));
        dto.setFullName(rs.getString("FullName"));
        dto.setCompany(rs.getString("Company"));
        dto.setGender(rs.getString("Gender"));
        dto.setDateOfBirth(rs.getDate("BirthDate") != null ? rs.getDate("BirthDate").toLocalDate() : null);
        dto.setEntryDate(rs.getDate("EntryDate") != null ? rs.getDate("EntryDate").toLocalDate() : null);
        dto.setExitDate(rs.getDate("ExitDate") != null ? rs.getDate("ExitDate").toLocalDate() : null);
        dto.setAddress(rs.getString("Address"));
        dto.setPhoneNumber(rs.getString("PhoneNumber"));
        dto.setJobTitle(rs.getString("JobTitle"));
        dto.setNote(rs.getString("Note"));
        dto.setStatus(EmployeeStatus.fromCode(rs.getInt("Status")).getLabel());
        dto.setDepartmentName(rs.getString("DepartmentName"));
        dto.setPositionName(rs.getString("PositionName"));
        // dto.setShiftName(null); // Optional: giữ dòng này nếu muốn khởi tạo shiftName là null
        dto.setManagerName(rs.getString("ManagerName"));
        return dto;
    };
}
