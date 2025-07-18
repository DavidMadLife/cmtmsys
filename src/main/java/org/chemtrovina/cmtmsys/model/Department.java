package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Department {

    private int departmentID;
    private String departmentName;

    public Department() {

    }
    public Department(int departmentID, String departmentName) {
        this.departmentID = departmentID;
        this.departmentName = departmentName;
    }
    public int getDepartmentID() {
        return departmentID;
    }
    public void setDepartmentID(int departmentID) {
        this.departmentID = departmentID;
    }
    public String getDepartmentName() {
        return departmentName;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Override
    public String toString() {
        return "Department{" +
                "departmentId=" + departmentID +
                "departmentName=" + departmentName +
                '}';

    }
}

