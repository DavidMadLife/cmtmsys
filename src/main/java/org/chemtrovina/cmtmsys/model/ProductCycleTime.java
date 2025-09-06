package org.chemtrovina.cmtmsys.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCycleTime {
    private int ctId;
    private int productId;      // FK -> Product
    private int warehouseId;    // FK -> Warehouse
    private BigDecimal ctSeconds;   // cycle time (giây)
    private int array;              // số array/module
    private boolean active;         // true = phiên bản đang dùng
    private Integer version;        // tùy chọn: tăng dần mỗi lần tạo mới
    private String note;            // ghi chú
    private LocalDateTime createdAt;

    public ProductCycleTime() {}

    public ProductCycleTime(int ctId, int productId, int warehouseId, BigDecimal ctSeconds,
                            int array, boolean active, Integer version,
                            String note, LocalDateTime createdAt) {
        this.ctId = ctId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.ctSeconds = ctSeconds;
        this.array = array;
        this.active = active;
        this.version = version;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getCtId() {
        return ctId;
    }
    public void setCtId(int ctId) {
        this.ctId = ctId;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public BigDecimal getCtSeconds() {
        return ctSeconds;
    }
    public void setCtSeconds(BigDecimal ctSeconds) {
        this.ctSeconds = ctSeconds;
    }

    public int getArray() {
        return array;
    }
    public void setArray(int array) {
        this.array = array;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
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
}
