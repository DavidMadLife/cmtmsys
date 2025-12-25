package org.chemtrovina.cmtmsys.dto;

import lombok.Data;

@Data
public class AbsentEmployeeDto {
    private int no;
    private int employeeId;
    private String employeeCode;
    private String fullName;
    private String departmentName;
    private String shiftCode;
    private String note;
}
