package org.chemtrovina.cmtmsys.model;


import java.time.LocalDateTime;

public class RejectedMaterial {
    private int id;
    private int workOrderId;
    private int warehouseId;
    private String sapCode;
    private int quantity;
    private LocalDateTime createdDate;
    private String note;

    public RejectedMaterial() {
    }
    public RejectedMaterial(int id, int workOrderId, int warehouseId,String sapCode, int quantity, LocalDateTime createdDate, String note) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.warehouseId = warehouseId;
        this.sapCode = sapCode;
        this.quantity = quantity;
        this.createdDate = createdDate;
        this.note = note;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getWorkOrderId() {
        return workOrderId;
    }
    public void setWorkOrderId(int workOrderId) {
        this.workOrderId = workOrderId;
    }
    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public LocalDateTime getCreatedDate() {

        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }
}
