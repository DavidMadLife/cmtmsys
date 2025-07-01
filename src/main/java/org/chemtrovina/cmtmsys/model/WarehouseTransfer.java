package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class WarehouseTransfer {
    private int transferId;
    private int workOrderId;
    private int fromWarehouseId;
    private int toWarehouseId;
    private LocalDateTime transferDate;
    private String note;
    private String employeeId;

    public WarehouseTransfer() {}

    public WarehouseTransfer(int transferId, int workOrderId, int fromWarehouseId, int toWarehouseId,
                             LocalDateTime transferDate, String note, String employeeId) {
        this.transferId = transferId;
        this.workOrderId = workOrderId;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.transferDate = transferDate;
        this.note = note;
        this.employeeId = employeeId;
    }

    // Getters and setters

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public int getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(int workOrderId) { this.workOrderId = workOrderId; }

    public int getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(int fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public int getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(int toWarehouseId) { this.toWarehouseId = toWarehouseId; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    @Override
    public String toString() {
        return "WarehouseTransfer{" +
                "transferId=" + transferId +
                ", workOrderId=" + workOrderId +
                ", fromWarehouseId=" + fromWarehouseId +
                ", toWarehouseId=" + toWarehouseId +
                ", transferDate=" + transferDate +
                ", note='" + note + '\'' +
                ", employeeId='" + employeeId + '\'' +
                '}';
    }
}
