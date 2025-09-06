package org.chemtrovina.cmtmsys.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCycleTimeViewDto {
    private int ctId;
    private String productCode;
    private String modelType;
    private String lineName;
    private int array;
    private BigDecimal ctSeconds;
    private Integer version;
    private boolean active;
    private LocalDateTime createdAt;

    // getters/setters
    public int getCtId() { return ctId; }
    public void setCtId(int ctId) { this.ctId = ctId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public int getArray() { return array; }
    public void setArray(int array) { this.array = array; }
    public BigDecimal getCtSeconds() { return ctSeconds; }
    public void setCtSeconds(BigDecimal ctSeconds) { this.ctSeconds = ctSeconds; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
