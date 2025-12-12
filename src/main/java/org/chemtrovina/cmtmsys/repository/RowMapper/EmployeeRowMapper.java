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
        return new Employee(
                rs.getInt("EmployeeId"),
                rs.getString("MSCNID1"),
                rs.getString("MSCNID2"),
                rs.getString("FullName"),
                rs.getString("Company"),
                rs.getString("Gender"),
                rs.getObject("BirthDate", LocalDate.class),
                rs.getString("Address"),
                rs.getString("PhoneNumber"),
                rs.getObject("ExitDate", LocalDate.class),
                rs.getInt("DepartmentId"),
                rs.getInt("PositionId"),
                rs.getString("Manager"),
                rs.getObject("EntryDate", LocalDate.class),
                rs.getString("JobTitle"),
                rs.getString("Note"),
                EmployeeStatus.fromCode(rs.getInt("Status"))

        );
    }
}
