package org.chemtrovina.cmtmsys.dto;

public class DepartmentSummaryDto {
    private String department;
    private int total;
    private int chem;
    private int tv;

    public DepartmentSummaryDto(String department) {
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getTotal() {
        return total;
    }

    public int getChem() {
        return chem;
    }

    public int getTv() {
        return tv;
    }

    public void add(boolean isChem) {
        if (isChem) {
            chem++;
        } else {
            tv++;
        }
        total++;
    }
}
