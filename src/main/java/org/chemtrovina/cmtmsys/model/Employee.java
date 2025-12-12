package org.chemtrovina.cmtmsys.model;

import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;

import java.time.LocalDate;

public class Employee {
    private int employeeId;
    private String MSCNID1;
    private String MSCNID2;
    private String fullName;
    private String company;
    private String gender;
    private LocalDate birthDate;
    private LocalDate entryDate;
    private String address;
    private String phoneNumber;
    private int departmentId;
    private int positionId;

    // 🔥 Đổi từ int managerId → String manager
    private String manager;

    private String jobTitle;
    private String note;
    private EmployeeStatus status;
    private LocalDate exitDate;

    public Employee() {}

    public Employee(int employeeId, String MSCNID1, String MSCNID2, String fullName, String company,
                    String gender, LocalDate birthDate, String address, String phoneNumber, LocalDate exitDate,
                    int departmentId, int positionId, String manager, LocalDate entryDate,
                    String jobTitle, String note, EmployeeStatus status) {

        this.employeeId = employeeId;
        this.MSCNID1 = MSCNID1;
        this.MSCNID2 = MSCNID2;
        this.fullName = fullName;
        this.company = company;
        this.gender = gender;
        this.birthDate = birthDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.departmentId = departmentId;
        this.positionId = positionId;

        this.manager = manager; // 🔥 changed

        this.entryDate = entryDate;

        this.jobTitle = jobTitle;
        this.note = note;
        this.status = status;
        this.exitDate = exitDate;
    }

    public String getManager() {
        return manager;
    }
    public void setManager(String manager) {
        this.manager = manager;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }
    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
    }

    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public EmployeeStatus getStatus() {
        return status;
    }
    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }
    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public int getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getMSCNID1() {
        return MSCNID1;
    }
    public void setMSCNID1(String MSCNID1) {
        this.MSCNID1 = MSCNID1;
    }

    public String getMSCNID2() {
        return MSCNID2;
    }
    public void setMSCNID2(String MSCNID2) {
        this.MSCNID2 = MSCNID2;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getPositionId() {
        return positionId;
    }
    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }


    @Override
    public String toString() {
        return "Employee{" +
                "employeeId=" + employeeId +
                ", MSCNID1='" + MSCNID1 + '\'' +
                ", MSCNID2='" + MSCNID2 + '\'' +
                ", fullName='" + fullName + '\'' +
                ", company='" + company + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", departmentId=" + departmentId +
                ", positionId=" + positionId +
                ", manager='" + manager + '\'' +     // 🔥 updated
                '}';
    }
}
