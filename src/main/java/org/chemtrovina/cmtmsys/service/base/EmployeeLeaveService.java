package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.EmployeeLeaveFilter;
import org.chemtrovina.cmtmsys.dto.LeaveStatisticDeptDto;
import org.chemtrovina.cmtmsys.model.EmployeeLeave;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeLeaveService {

    EmployeeLeave getLeaveByEmployeeAndDate(int employeeId, LocalDate date);

    List<EmployeeLeave> getLeavesByEmployeeAndRange(
            int employeeId,
            LocalDate from,
            LocalDate to
    );

    void create(EmployeeLeave leave);

    void update(EmployeeLeave leave);

    void delete(int leaveId);

    List<LeaveStatisticDeptDto> statisticByDepartment(
            LocalDate fromDate,
            LocalDate toDate
    );
    List<EmployeeLeave> findByFilter(EmployeeLeaveFilter filter);

}
