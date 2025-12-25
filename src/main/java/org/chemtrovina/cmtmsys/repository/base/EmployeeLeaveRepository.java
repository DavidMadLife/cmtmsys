package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.EmployeeLeaveFilter;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.EmployeeLeave;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveRepository {

    EmployeeLeave findByEmployeeAndDate(int employeeId, LocalDate date);

    List<EmployeeLeave> findByEmployeeAndDateRange(
            int employeeId,
            LocalDate from,
            LocalDate to
    );

    int insert(EmployeeLeave leave);

    int update(EmployeeLeave leave);

    int delete(int leaveId);
    List<LeaveStatisticDeptDto> statisticByDepartment(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<EmployeeLeave> findByFilter(EmployeeLeaveFilter filter);

    List<EmployeeLeave> findLeaveByDate(LocalDate date);



}
