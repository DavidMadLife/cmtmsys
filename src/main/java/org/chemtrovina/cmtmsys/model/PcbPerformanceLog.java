package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class PcbPerformanceLog {
    private int logId;                 // ID bản ghi
    private int productId;            // FK tới Product
    private int warehouseId;               // FK tới Line (tên cũ: warehouse)

    private String carrierId;         // Mã PCB (Barcode)
    private String aoiMachineCode;    // Mã máy AOI chạy log

    private int totalModules;         // Tổng số array/module
    private int ngModules;            // Số NG
    private double performance;       // (OK/Total) * 100

    private String logFileName;       // Tên file log (trace)
    private LocalDateTime createdAt;  // Thời điểm xử lý

    public PcbPerformanceLog() {}

    public PcbPerformanceLog(int logId, int productId, int warehouseId, String carrierId, String aoiMachineCode, int totalModules, int ngModules, double performance, String logFileName, LocalDateTime createdAt) {
        this.logId = logId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.carrierId = carrierId;
        this.aoiMachineCode = aoiMachineCode;
        this.totalModules = totalModules;
        this.ngModules = ngModules;
        this.performance = performance;
        this.logFileName = logFileName;
        this.createdAt = createdAt;
    }

    // Getter & Setter 
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
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

    public String getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(String carrierId) {
        this.carrierId = carrierId;
    }

    public String getAoiMachineCode() {
        return aoiMachineCode;
    }

    public void setAoiMachineCode(String aoiMachineCode) {
        this.aoiMachineCode = aoiMachineCode;
    }

    public int getTotalModules() {
        return totalModules;
    }

    public void setTotalModules(int totalModules) {
        this.totalModules = totalModules;
    }

    public int getNgModules() {
        return ngModules;
    }

    public void setNgModules(int ngModules) {
        this.ngModules = ngModules;
    }

    public double getPerformance() {
        return performance;
    }

    public void setPerformance(double performance) {
        this.performance = performance;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
