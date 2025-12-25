package org.chemtrovina.cmtmsys.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.chemtrovina.cmtmsys.model.enums.LeaveType;

@Data
public class EmployeeLeave {

    private int leaveId;

    private int employeeId;

    private LocalDate fromDate;   // ngày bắt đầu nghỉ
    private LocalDate toDate;     // ngày kết thúc nghỉ

    private LeaveType leaveType;

    private String reason;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}
