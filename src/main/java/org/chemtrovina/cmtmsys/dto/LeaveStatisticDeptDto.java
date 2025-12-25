package org.chemtrovina.cmtmsys.dto;

import lombok.Data;

@Data
public class LeaveStatisticDeptDto {

    private String departmentName;

    private int leavePermit;      // Nghỉ phép
    private int leaveNoPermit;    // Nghỉ không phép
    private int leaveSick;        // Nghỉ bệnh
    private int leavePrivate;
    private int leaveOther;

    public int getTotal() {
        return leavePermit + leaveNoPermit + leaveSick + leavePrivate + leaveOther;
    }
}
