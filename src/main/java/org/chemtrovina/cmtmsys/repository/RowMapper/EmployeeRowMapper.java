package org.chemtrovina.cmtmsys.repository.RowMapper;

import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class EmployeeRowMapper implements RowMapper<Employee> {

    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee emp = new Employee();

        emp.setEmployeeId(rs.getInt("EmployeeId"));
        emp.setMSCNID1(rs.getString("MSCNID1"));
        emp.setMSCNID2(rs.getString("MSCNID2"));
        emp.setFullName(rs.getString("FullName"));
        emp.setCompany(rs.getString("Company"));
        emp.setGender(rs.getString("Gender"));
        emp.setBirthDate(rs.getObject("BirthDate", LocalDate.class));
        emp.setEntryDate(rs.getObject("EntryDate", LocalDate.class));
        emp.setExitDate(rs.getObject("ExitDate", LocalDate.class));
        emp.setAddress(rs.getString("Address"));
        emp.setPhoneNumber(rs.getString("PhoneNumber"));

        emp.setDepartmentName(rs.getString("DepartmentName")); // ✅ NEW
        emp.setPositionName(rs.getString("PositionName"));     // ✅ NEW

        emp.setManager(rs.getString("Manager"));
        emp.setJobTitle(rs.getString("JobTitle"));
        emp.setNote(rs.getString("Note"));
        emp.setStatus(EmployeeStatus.fromCode(rs.getInt("Status")));

        return emp;
    }

}
