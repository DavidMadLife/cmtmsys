package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftPlanEmployee {

    private int shiftPlanId;       // PK
    private int employeeId;        // FK Employee
    private LocalDate shiftDate;   // ngày phân ca

    private String shiftCode;      // ca kế hoạch (A/B/C/OFF…)
    private String shiftActual;    // ca thực tế
    private String note;           // ghi chú (đổi ca, OT…)

    private String importedBy;
    private LocalDateTime importedAt;

    public ShiftPlanEmployee() {}

    public ShiftPlanEmployee(int shiftPlanId, int employeeId, LocalDate shiftDate,
                             String shiftCode, String shiftActual, String note,
                             String importedBy, LocalDateTime importedAt) {

        this.shiftPlanId = shiftPlanId;
        this.employeeId = employeeId;
        this.shiftDate = shiftDate;
        this.shiftCode = shiftCode;
        this.shiftActual = shiftActual;
        this.note = note;
        this.importedBy = importedBy;
        this.importedAt = importedAt;
    }

    public int getShiftPlanId() {
        return shiftPlanId;
    }

    public void setShiftPlanId(int shiftPlanId) {
        this.shiftPlanId = shiftPlanId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftActual() {
        return shiftActual;
    }

    public void setShiftActual(String shiftActual) {
        this.shiftActual = shiftActual;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(String importedBy) {
        this.importedBy = importedBy;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }
}
