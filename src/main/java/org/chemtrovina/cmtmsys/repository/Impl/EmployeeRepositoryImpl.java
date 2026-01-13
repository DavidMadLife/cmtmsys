package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.repository.RowMapper.EmployeeRowMapper;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class EmployeeRepositoryImpl extends GenericRepositoryImpl<Employee> implements EmployeeRepository {

    private final BeanPropertyRowMapper<Employee> mapper =
            new BeanPropertyRowMapper<>(Employee.class);
    public EmployeeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new EmployeeRowMapper(), "Employee");
    }

    @Override
    public void add(Employee e) {
        String sql = """
        INSERT INTO Employee (
            MSCNID1, MSCNID2, FullName, Company, Gender,
            BirthDate, EntryDate, ExitDate,
            Address, PhoneNumber,
            DepartmentName, PositionName,
            Manager, JobTitle, Note, Status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(sql,
                e.getMSCNID1(),
                e.getMSCNID2(),
                e.getFullName(),
                e.getCompany(),
                e.getGender(),
                e.getBirthDate(),
                e.getEntryDate(),
                e.getExitDate(),
                e.getAddress(),
                e.getPhoneNumber(),
                e.getDepartmentName(),
                e.getPositionName(),
                e.getManager(),
                e.getJobTitle(),
                e.getNote(),
                e.getStatus().getCode()
        );
    }


    @Override
    public Employee findByFullName(String fullName) {

        String sql = """
        SELECT *
        FROM Employee
        WHERE FullName = ?
    """;

        var list = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Employee.class),
                fullName
        );

        return list.isEmpty() ? null : list.get(0);
    }


    @Override
    public void update(Employee e) {
        String sql = """
        UPDATE Employee SET
            MSCNID1 = ?, MSCNID2 = ?, FullName = ?, Company = ?, Gender = ?,
            BirthDate = ?, EntryDate = ?, ExitDate = ?,
            Address = ?, PhoneNumber = ?,
            DepartmentName = ?, PositionName = ?,
            Manager = ?, JobTitle = ?, Note = ?, Status = ?
        WHERE EmployeeId = ?
    """;

        jdbcTemplate.update(sql,
                e.getMSCNID1(),
                e.getMSCNID2(),
                e.getFullName(),
                e.getCompany(),
                e.getGender(),
                e.getBirthDate(),
                e.getEntryDate(),
                e.getExitDate(),
                e.getAddress(),
                e.getPhoneNumber(),
                e.getDepartmentName(),
                e.getPositionName(),
                e.getManager(),
                e.getJobTitle(),
                e.getNote(),
                e.getStatus().getCode(),
                e.getEmployeeId()
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
        return jdbcTemplate.query(sql, mapper, employeeId)
                .stream().findFirst().orElse(null);
    }


    @Override
    public List<Employee> findAll() {
        return jdbcTemplate.query("SELECT * FROM Employee", mapper);
    }


    @Override
    public List<Employee> findAllActive() {
        return jdbcTemplate.query("""
        SELECT *
        FROM Employee
        WHERE Status = ?
          AND ExitDate IS NULL
    """, mapper, EmployeeStatus.ACTIVE.getCode());
    }


    @Override
    public List<Employee> findAllActiveByDate(LocalDate date) {
        return jdbcTemplate.query("""
        SELECT *
        FROM Employee
        WHERE Status = ?
          AND (ExitDate IS NULL OR ExitDate > ?)
    """, mapper, EmployeeStatus.ACTIVE.getCode(), date);
    }





    @Override
    public List<EmployeeDto> findAllEmployeeDtos() {
        String sql = """
        SELECT
            EmployeeID,
            MSCNID1,
            MSCNID2,
            FullName,
            Company,
            Gender,
            BirthDate,
            EntryDate,
            ExitDate,
            Address,
            PhoneNumber,
            DepartmentName,
            PositionName,
            Manager AS ManagerName,
            JobTitle,
            Note,
            Status
        FROM Employee
        ORDER BY EmployeeID
    """;

        return jdbcTemplate.query(sql, employeeDtoMapper);
    }


    public List<EmployeeDto> findFilteredEmployeeDtos(
            EmployeeStatus status,
            LocalDate entryDateFrom,
            LocalDate entryDateTo
    ) {
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
            e.Manager AS ManagerName,
            e.DepartmentName,
            e.PositionName
        FROM Employee e
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

        return jdbcTemplate.query(
                sql.toString(),
                params.toArray(),
                employeeDtoMapper
        );
    }


    ////////////////////////////////////Mapper////////////////////////////////////////////
    private final RowMapper<EmployeeDto> employeeDtoMapper = (rs, rowNum) -> {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmployeeId(rs.getInt("EmployeeID"));
        dto.setNo(rowNum + 1);
        dto.setMscnId1(rs.getString("MSCNID1"));
        dto.setMscnId2(rs.getString("MSCNID2"));
        dto.setFullName(rs.getString("FullName"));
        dto.setCompany(rs.getString("Company"));
        dto.setGender(rs.getString("Gender"));
        dto.setBirthDate(rs.getDate("BirthDate") != null ? rs.getDate("BirthDate").toLocalDate() : null);
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


    @Override
    public Employee findByMscnId1(String mscnId1) {
        if (mscnId1 == null || mscnId1.isBlank()) return null;
        String sql = "SELECT TOP 1 * FROM Employee WHERE MSCNID1 = ?";
        return jdbcTemplate.query(sql, mapper, mscnId1.trim())
                .stream().findFirst().orElse(null);
    }


    @Override
    public Employee findByMscnId2(String mscnId2) {
        if (mscnId2 == null || mscnId2.isBlank()) return null;
        String sql = "SELECT TOP 1 * FROM Employee WHERE MSCNID1 = ?";
        return jdbcTemplate.query(sql, mapper, mscnId2.trim())
                .stream().findFirst().orElse(null);
    }


    // Trong EmployeeRepositoryImpl
    public Employee findByCardId(String cardId) {
        String sql = "SELECT * FROM Employee WHERE MSCNID1 = ? OR MSCNID2 = ?";
        List<Employee> results = jdbcTemplate.query(sql, mapper, cardId, cardId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public void updateManager(int employeeId, String managerName) {
        String sql = "UPDATE Employee SET Manager = ? WHERE EmployeeId = ?";
        jdbcTemplate.update(sql, managerName, employeeId);
    }

    @Override
    public List<Employee> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        // Tạo chuỗi tham số cho mệnh đề IN (?, ?, ?)
        String inSql = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format("""
            SELECT *                
            FROM Employee
            WHERE EmployeeId IN (%s)
        """, inSql);

        return jdbcTemplate.query(sql, mapper, ids.toArray());
    }

    @Override
    public void batchInsert(List<Employee> employees) {
        if (employees.isEmpty()) return;

        String sql = """
        INSERT INTO Employee (
            MSCNID1, MSCNID2, FullName, Company, Gender,
            BirthDate, EntryDate, ExitDate,
            Address, PhoneNumber,
            DepartmentName, PositionName,
            Manager, JobTitle, Note, Status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.batchUpdate(sql, employees, 100, (ps, e) -> {
            ps.setString(1, e.getMSCNID1());
            ps.setString(2, e.getMSCNID2());
            ps.setString(3, e.getFullName());
            ps.setString(4, e.getCompany());
            ps.setString(5, e.getGender());
            ps.setObject(6, e.getBirthDate());
            ps.setObject(7, e.getEntryDate());
            ps.setObject(8, e.getExitDate());
            ps.setString(9, e.getAddress());
            ps.setString(10, e.getPhoneNumber());
            ps.setString(11, e.getDepartmentName());
            ps.setString(12, e.getPositionName());
            ps.setString(13, e.getManager());
            ps.setString(14, e.getJobTitle());
            ps.setString(15, e.getNote());
            ps.setInt(16, e.getStatus().getCode());
        });
    }

    @Override
    public void batchMarkInactiveByIds(List<Integer> employeeIds, LocalDate exitDate) {
        if (employeeIds == null || employeeIds.isEmpty()) return;

        String sql = """
        UPDATE Employee
        SET Status = ?, ExitDate = ?
        WHERE EmployeeId = ?
    """;

        jdbcTemplate.batchUpdate(sql, employeeIds, 200, (ps, empId) -> {
            ps.setInt(1, EmployeeStatus.INACTIVE.getCode());
            ps.setObject(2, exitDate);
            ps.setInt(3, empId);
        });
    }


    @Override
    public void batchUpdate(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) return;

        String sql = """
        UPDATE Employee SET
            MSCNID1 = ?,
            MSCNID2 = ?,
            FullName = ?,
            Company = ?,
            Gender = ?,
            BirthDate = ?,
            EntryDate = ?,
            ExitDate = ?,
            Address = ?,
            PhoneNumber = ?,
            DepartmentName = ?,
            PositionName = ?,
            Manager = ?,
            JobTitle = ?,
            Note = ?,
            Status = ?
        WHERE EmployeeId = ?
    """;

        jdbcTemplate.batchUpdate(sql, employees, 100, (ps, e) -> {
            ps.setString(1, e.getMSCNID1());
            ps.setString(2, e.getMSCNID2());
            ps.setString(3, e.getFullName());
            ps.setString(4, e.getCompany());
            ps.setString(5, e.getGender());
            ps.setObject(6, e.getBirthDate());
            ps.setObject(7, e.getEntryDate());
            ps.setObject(8, e.getExitDate());
            ps.setString(9, e.getAddress());
            ps.setString(10, e.getPhoneNumber());

            // ✅ dùng NAME, không dùng ID
            ps.setString(11, e.getDepartmentName());
            ps.setString(12, e.getPositionName());

            ps.setString(13, e.getManager());
            ps.setString(14, e.getJobTitle());
            ps.setString(15, e.getNote());
            ps.setInt(16, e.getStatus().getCode());
            ps.setInt(17, e.getEmployeeId());
        });
    }




}
