package org.chemtrovina.cmtmsys.model;

import lombok.Data;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.time.LocalDate;
@Data
public class Employee {
    private int employeeId;
    private String MSCNID1;
    private String MSCNID2;
    private String fullName;
    private String company;
    private String gender;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private LocalDate exitDate;
    private String address;
    private String phoneNumber;

    private String departmentName;
    private String positionName;
    private String manager;

    private String jobTitle;
    private String note;
    private EmployeeStatus status;
}
