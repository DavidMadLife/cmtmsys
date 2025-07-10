package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class RejectedMaterialDto {
    private int id;
    private String workOrderCode;
    private String sapCode;
    private int quantity;
    private String warehouseName;
    private String note;
    private LocalDateTime createdDate;

    // Getter, Setter

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getWorkOrderCode() {
        return workOrderCode;
    }
    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getWarehouseName() {
        return warehouseName;
    }
    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
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
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

}
