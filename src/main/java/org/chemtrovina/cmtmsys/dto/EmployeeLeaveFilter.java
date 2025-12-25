package org.chemtrovina.cmtmsys.dto;

import lombok.Data;
import org.chemtrovina.cmtmsys.model.enums.LeaveType;

@Data
public class EmployeeLeaveFilter extends Filter {

    private Integer employeeId;
    private LeaveType leaveType;
}
