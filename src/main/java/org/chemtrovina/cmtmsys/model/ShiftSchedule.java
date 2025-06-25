package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftSchedule {
    private int id;
    private int employeeId;
    private LocalDate workDate;
    private int shiftId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShiftSchedule() {

    }
    public ShiftSchedule(int id, int employeeId, LocalDate workDate, int shiftId, String note, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.shiftId = shiftId;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }
    public LocalDate getWorkDate() {
        return workDate;
    }
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }
    public int getShiftId() {
        return shiftId;
    }
    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
