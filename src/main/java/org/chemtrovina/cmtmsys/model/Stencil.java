package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Stencil {
    private int stencilId;
    private String barcode;
    private String stencilNo;
    private int productId;              // FK -> Product
    private Integer currentWarehouseId; // FK -> Warehouse (nullable)
    private String versionLabel;
    private String size;                // ví dụ "650x550"
    private int arrayCount;
    private LocalDate receivedDate;
    private String status;              // InStock, InUse, Locked, Retired, Scrap
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stencil() {
        // no-arg constructor (cần cho JPA, Spring, Jackson...)
    }

    // All-args constructor
    public Stencil(int stencilId, String barcode, String stencilNo, int productId,
                   Integer currentWarehouseId, String versionLabel, String size,
                   int arrayCount, LocalDate receivedDate, String status,
                   String note, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.stencilId = stencilId;
        this.barcode = barcode;
        this.stencilNo = stencilNo;
        this.productId = productId;
        this.currentWarehouseId = currentWarehouseId;
        this.versionLabel = versionLabel;
        this.size = size;
        this.arrayCount = arrayCount;
        this.receivedDate = receivedDate;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter & Setter
    public int getStencilId() {
        return stencilId;
    }
    public void setStencilId(int stencilId) {
        this.stencilId = stencilId;
    }

    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStencilNo() {
        return stencilNo;
    }
    public void setStencilNo(String stencilNo) {
        this.stencilNo = stencilNo;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Integer getCurrentWarehouseId() {
        return currentWarehouseId;
    }
    public void setCurrentWarehouseId(Integer currentWarehouseId) {
        this.currentWarehouseId = currentWarehouseId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }
    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public int getArrayCount() {
        return arrayCount;
    }
    public void setArrayCount(int arrayCount) {
        this.arrayCount = arrayCount;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }
    public void setReceivedDate(LocalDate receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
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
