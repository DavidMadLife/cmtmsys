package org.chemtrovina.cmtmsys.dto;

import lombok.Data;


public record AttendanceImportRow(
        int rowIndex,
        String employeeCode,
        String fullName,
        String shiftCode,
        String inTime,
        String outTime
){}


