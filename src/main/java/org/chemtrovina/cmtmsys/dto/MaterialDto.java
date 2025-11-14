package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class MaterialDto {
    private int materialId;           // 🆕 ID để thao tác delete/update
    private String sapCode;
    private String rollCode;
    private int quantity;
    private String warehouseName;
    private String spec;
    private LocalDateTime createdAt;
    private String employeeId;
    private String lot;
    private String maker;             // ✅ thêm mới

    // =====================================================================
    // ⚙️ Constructors
    // =====================================================================
    public MaterialDto() {
        // constructor mặc định (bắt buộc cho JavaFX, Jackson...)
    }

    public MaterialDto(int materialId, String sapCode, String rollCode, int quantity,
                       String warehouseName, String spec, LocalDateTime createdAt,
                       String employeeId, String lot, String maker) { // ✅ thêm maker
        this.materialId = materialId;
        this.sapCode = sapCode;
        this.rollCode = rollCode;
        this.quantity = quantity;
        this.warehouseName = warehouseName;
        this.spec = spec;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
        this.lot = lot;
        this.maker = maker; // ✅
    }

    // =====================================================================
    // 🧱 Getters
    // =====================================================================
    public int getMaterialId() {
        return materialId;
    }

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

    public String getLot() {
        return lot;
    }

    public String getMaker() {        // ✅ thêm getter
        return maker;
    }

    // =====================================================================
    // 🧭 Setters
    // =====================================================================
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }

    public void setRollCode(String rollCode) {
        this.rollCode = rollCode;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public void setMaker(String maker) { // ✅ thêm setter
        this.maker = maker;
    }
}
