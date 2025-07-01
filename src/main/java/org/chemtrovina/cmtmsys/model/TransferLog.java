package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class TransferLog {
    private int transferId;
    private String rollCode;
    private int fromWarehouseId;
    private int toWarehouseId;
    private LocalDateTime transferDate;
    private String note;
    private String employeeId;

    public TransferLog() {

    }

    public TransferLog(int transferId, String rollCode, int fromWarehouseId, int toWarehouseId,
                       LocalDateTime transferDate, String note, String employeeId) {
        this.transferId = transferId;
        this.rollCode = rollCode;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.transferDate = transferDate;
        this.note = note;
        this.employeeId = employeeId;
    }

    // Getters & setters

    public int getTransferId() {
        return transferId;
    }
    public String getRollCode() {
        return rollCode;
    }
    public int getFromWarehouseId() {
        return fromWarehouseId;
    }
    public int getToWarehouseId() {
        return toWarehouseId;
    }
    public LocalDateTime getTransferDate() {
        return transferDate;
    }
    public String getNote() {
        return note;
    }

    public void setFromWarehouseId(int fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }
    public void setToWarehouseId(int toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }
    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }
    public void setRollCode(String rollCode) {
        this.rollCode = rollCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
