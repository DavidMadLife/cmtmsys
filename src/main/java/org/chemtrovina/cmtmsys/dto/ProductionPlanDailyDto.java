package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlanDailyDto {
    private int dailyID;
    private int planItemID;
    private LocalDate runDate;
    private int quantity;
    private int actualQuantity;
    private String modelType;     // <-- thêm
    private String productCode;   // <-- thường cũng cần cho UI
    private String modelName;     // optional

    private LocalDateTime createdAt;

    // getters/setters
    public int getDailyID() { return dailyID; }
    public void setDailyID(int dailyID) { this.dailyID = dailyID; }

    public int getPlanItemID() { return planItemID; }
    public void setPlanItemID(int planItemID) { this.planItemID = planItemID; }

    public LocalDate getRunDate() { return runDate; }
    public void setRunDate(LocalDate runDate) { this.runDate = runDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
