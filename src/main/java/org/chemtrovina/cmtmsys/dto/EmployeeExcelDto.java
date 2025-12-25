package org.chemtrovina.cmtmsys.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class EmployeeExcelDto {

    private String mscnId1;
    private String fullName;
    private String company;
    private String departmentName;
    private String gender;
    private String positionName;
    private String jobTitle;
    private String manager;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private String phoneNumber;
    private String note;

    // getter / setter
}
