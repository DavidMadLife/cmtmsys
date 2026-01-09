package org.chemtrovina.cmtmsys.dto;

import lombok.Data;

@Data
public class EmployeeScanViewDto {
    private int id;
    private Integer no;
    private String employeeCode;
    private String fullName;
    private String scanType;   // IN / OUT
    private String scanTime;   // HH:mm
}
