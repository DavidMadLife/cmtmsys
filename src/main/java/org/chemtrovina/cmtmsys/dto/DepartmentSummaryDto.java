package org.chemtrovina.cmtmsys.dto;

import java.util.HashMap;
import java.util.Map;

public class DepartmentSummaryDto {
    private String department;
    private int total;
    private int chem;
    private int tv;

    private final Map<String, Integer> companyCounts = new HashMap<>();
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

    public Map<String, Integer> getCompanyCounts() {
        return companyCounts;
    }

    public void addCompany(String company) {
        companyCounts.merge(company, 1, Integer::sum);
    }
}
