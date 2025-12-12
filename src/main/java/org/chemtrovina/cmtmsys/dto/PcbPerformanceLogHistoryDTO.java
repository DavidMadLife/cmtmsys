package org.chemtrovina.cmtmsys.dto;

import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDateTime;

public class PcbPerformanceLogHistoryDTO {
    private int logId;
    private String modelCode;
    private ModelType modelType;
    private String carrierId;
    private String aoi;
    private int totalModules;
    private int ngModules;
    private double performance;
    private String logFileName;
    private LocalDateTime createdAt;
    private String warehouseName;
    private double timeDiffSeconds;


    public PcbPerformanceLogHistoryDTO() {}

    public PcbPerformanceLogHistoryDTO(int logId,String modelCode, ModelType modelType, String carrierId, String aoi,
                                       int totalModules, int ngModules, double performance, String logFileName,
                                       LocalDateTime createdAt, String warehouseName) {
        this.logId = logId;
        this.modelCode = modelCode;
        this.modelType = modelType;
        this.carrierId = carrierId;
        this.aoi = aoi;
        this.totalModules = totalModules;
        this.ngModules = ngModules;
        this.performance = performance;
        this.logFileName = logFileName;
        this.createdAt = createdAt;
        this.warehouseName = warehouseName;
    }

    public int getLogId() {
        return logId;
    }
    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public String getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(String carrierId) {
        this.carrierId = carrierId;
    }

    public String getAoi() {
        return aoi;
    }

    public void setAoi(String aoi) {
        this.aoi = aoi;
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

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public double getTimeDiffSeconds() { return timeDiffSeconds; }
    public void setTimeDiffSeconds(double timeDiffSeconds) { this.timeDiffSeconds = timeDiffSeconds; }
}
