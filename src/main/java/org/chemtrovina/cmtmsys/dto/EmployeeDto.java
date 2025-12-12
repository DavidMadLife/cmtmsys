package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDate;

public class EmployeeDto {
    private int employeeId;   // 🔥 thêm ID
    private int no;
    private String mscnId1;
    private String mscnId2;
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
    private String shiftName;
    private String managerName;
    private String jobTitle;
    private String note;
    private String status;

    // ================= Constructor mặc định =================
    public EmployeeDto() {
    }

    // ================= Constructor đầy đủ =================
    public EmployeeDto(int employeeId, int no, String mscnId1, String mscnId2, String fullName, String company,
                       String gender, LocalDate birthDate, LocalDate entryDate, LocalDate exitDate,
                       String address, String phoneNumber, String departmentName, String positionName,
                       String shiftName, String managerName, String jobTitle, String note, String status) {

        this.employeeId = employeeId;   // 🔥
        this.no = no;
        this.mscnId1 = mscnId1;
        this.mscnId2 = mscnId2;
        this.fullName = fullName;
        this.company = company;
        this.gender = gender;
        this.birthDate = birthDate;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.departmentName = departmentName;
        this.positionName = positionName;
        this.shiftName = shiftName;
        this.managerName = managerName;
        this.jobTitle = jobTitle;
        this.note = note;
        this.status = status;
    }

    // ================= Getter & Setter =================

    public int getEmployeeId() {      // 🔥
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {   // 🔥
        this.employeeId = employeeId;
    }

    public int getNo() { return no; }
    public void setNo(int no) { this.no = no; }

    public String getMscnId1() { return mscnId1; }
    public void setMscnId1(String mscnId1) { this.mscnId1 = mscnId1; }

    public String getMscnId2() { return mscnId2; }
    public void setMscnId2(String mscnId2) { this.mscnId2 = mscnId2; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "employeeId=" + employeeId +    // 🔥
                ", no=" + no +
                ", mscnId1='" + mscnId1 + '\'' +
                ", mscnId2='" + mscnId2 + '\'' +
                ", fullName='" + fullName + '\'' +
                ", company='" + company + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", entryDate=" + entryDate +
                ", exitDate=" + exitDate +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", positionName='" + positionName + '\'' +
                ", shiftName='" + shiftName + '\'' +
                ", managerName='" + managerName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
