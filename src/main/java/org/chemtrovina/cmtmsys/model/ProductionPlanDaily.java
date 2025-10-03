package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlanDaily {
    private int dailyID;
    private int planItemID;
    private LocalDate runDate;
    private int quantity;
    private LocalDateTime createdAt;
    private int actualQuantity;

    private String modelType;

    public ProductionPlanDaily() {}

    public ProductionPlanDaily(int dailyID, int planItemID, LocalDate runDate, int quantity, LocalDateTime createdAt, int actualQuantity) {
        this.dailyID = dailyID;
        this.planItemID = planItemID;
        this.runDate = runDate;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.actualQuantity = actualQuantity;
    }

    // Getters and Setters
    public int getDailyID() { return dailyID; }
    public void setDailyID(int dailyID) { this.dailyID = dailyID; }

    public int getPlanItemID() { return planItemID; }
    public void setPlanItemID(int planItemID) { this.planItemID = planItemID; }

    public LocalDate getRunDate() { return runDate; }

    public void setRunDate(LocalDate runDate) { this.runDate = runDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getActualQuantity() { return actualQuantity; }
    public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
}
