package org.chemtrovina.cmtmsys.model;

import org.chemtrovina.cmtmsys.model.enums.ScanAction; // Import Enum cho hành động (IN/OUT)
import org.chemtrovina.cmtmsys.model.enums.ScanMethod; // Import Enum cho phương thức (Vân tay/Thẻ)

import java.time.LocalDateTime;

/**
 * Model ghi lại mọi lần chấm công thô (raw punch) từ thiết bị.
 * Đây là dữ liệu nguồn cho việc tính toán công.
 */
public class TimeAttendanceLog {

    private int logId;
    private int employeeId;
    private LocalDateTime scanDateTime = LocalDateTime.now();

    private ScanAction scanAction;
    private ScanMethod scanMethod;

    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================================================
    // Constructor (Empty and Full)
    // =========================================================

    public TimeAttendanceLog() {
    }

    public TimeAttendanceLog(int logId, int employeeId, LocalDateTime scanDateTime,
                             ScanAction scanAction, ScanMethod scanMethod, LocalDateTime createdAt) {
        this.logId = logId;
        this.employeeId = employeeId;
        this.scanDateTime = scanDateTime;
        this.scanAction = scanAction;
        this.scanMethod = scanMethod;
        this.createdAt = createdAt;
    }

    // =========================================================
    // Getters and Setters
    // =========================================================

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getScanDateTime() {
        return scanDateTime;
    }

    public void setScanDateTime(LocalDateTime scanDateTime) {
        this.scanDateTime = scanDateTime;
    }

    public ScanAction getScanAction() {
        return scanAction;
    }

    public void setScanAction(ScanAction scanAction) {
        this.scanAction = scanAction;
    }

    public void setScanActionFromString(String scanAction) {
        this.scanAction = ScanAction.valueOf(scanAction);
    }


    public ScanMethod getScanMethod() {
        return scanMethod;
    }

    public void setScanMethod(ScanMethod scanMethod) {
        this.scanMethod = scanMethod;
    }

    public void setScanMethodFromString(String scanMethod) {
        this.scanMethod = ScanMethod.valueOf(scanMethod);
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}