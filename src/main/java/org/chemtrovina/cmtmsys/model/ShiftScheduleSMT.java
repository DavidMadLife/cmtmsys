package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ShiftScheduleSMT {
    private int shiftId;
    private int warehouseId;
    private LocalDateTime shiftDate;   // ngày gốc
    private String shiftType;          // "DAY" / "NIGHT"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    public int getShiftId() {
        return shiftId;
    }
    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public LocalDateTime getShiftDate() {
        return shiftDate;
    }
    public void setShiftDate(LocalDateTime shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getShiftType() {
        return shiftType;
    }
    public void setShiftType(String shiftType) {
        this.shiftType = shiftType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
