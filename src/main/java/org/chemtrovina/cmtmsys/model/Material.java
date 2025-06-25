package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class Material {
    private int materialId;
    private String sapCode;
    private String rollCode;
    private int quantity;
    private int warehouseId;
    private LocalDateTime createdAt;
    private String spec;
    private String employeeId;

    public Material(int materialId, String sapCode, String rollCode, int quantity, int warehouseId, LocalDateTime createdAt, String spec, String employeeId) {
        this.materialId = materialId;
        this.sapCode = sapCode;
        this.rollCode = rollCode;
        this.quantity = quantity;
        this.warehouseId = warehouseId;
        this.createdAt = createdAt;
        this.spec = spec;
        this.employeeId = employeeId;
    }

    // Getters & setters
    public int getMaterialId() {
        return materialId;
    }
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
    public String getRollCode() {
        return rollCode;
    }
    public void setRollCode(String rollCode) {
        this.rollCode = rollCode;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getSpec() {
        return spec;
    }
    public void setSpec(String spec) {
        this.spec = spec;
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

}
