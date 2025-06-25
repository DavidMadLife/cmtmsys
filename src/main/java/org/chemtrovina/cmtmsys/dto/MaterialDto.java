package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class MaterialDto {
    private String sapCode;
    private String rollCode;
    private int quantity;
    private String warehouseName;
    private String spec;
    private LocalDateTime createdAt;
    private String employeeId;

    public MaterialDto(String sapCode, String rollCode, int quantity, String warehouseName, String spec, LocalDateTime createdAt, String employeeId) {
        this.sapCode = sapCode;
        this.rollCode = rollCode;
        this.quantity = quantity;
        this.warehouseName = warehouseName;
        this.spec = spec;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
    }

    // Getters
    public String getSapCode() {
        return sapCode;
    }

    public String getRollCode() {
        return rollCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getWarehouseName() {
        return warehouseName;
    }
    public String getSpec() {
        return spec;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public String getEmployeeId() {
        return employeeId;
    }

}
