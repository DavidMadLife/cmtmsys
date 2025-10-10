package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class EBoardPerformanceLog {
    private int logId;
    private int eBoardProductId;      // FK -> EBoardProduct
    private int setId;                // FK -> EBoardSet
    private int warehouseId;          // AOI line
    private String circuitType;       // LED / PD
    private String modelType;         // BOT / TOP
    private String carrierId;         // optional
    private String aoiMachineCode;    // optional
    private int totalModules;
    private int ngModules;
    private double performance;
    private String logFileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EBoardPerformanceLog() {}

    public EBoardPerformanceLog(int logId, int eBoardProductId, int setId, int warehouseId,
                                String circuitType, String modelType, String carrierId,
                                String aoiMachineCode, int totalModules, int ngModules,
                                double performance, String logFileName,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.logId = logId;
        this.eBoardProductId = eBoardProductId;
        this.setId = setId;
        this.warehouseId = warehouseId;
        this.circuitType = circuitType;
        this.modelType = modelType;
        this.carrierId = carrierId;
        this.aoiMachineCode = aoiMachineCode;
        this.totalModules = totalModules;
        this.ngModules = ngModules;
        this.performance = performance;
        this.logFileName = logFileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getLogId() {
        return logId;
    }
    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int geteBoardProductId() {
        return eBoardProductId;
    }
    public void seteBoardProductId(int eBoardProductId) {
        this.eBoardProductId = eBoardProductId;
    }

    public int getSetId() {
        return setId;
    }
    public void setSetId(int setId) {
        this.setId = setId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getCircuitType() {
        return circuitType;
    }
    public void setCircuitType(String circuitType) {
        this.circuitType = circuitType;
    }

    public String getModelType() {
        return modelType;
    }
    public void setModelType(String modelType) {
        this.modelType = modelType;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
