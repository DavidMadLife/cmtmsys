package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ProductionPlanItem {
    private int planItemID;
    private int planID;
    private int productID;
    private int plannedQuantity;
    private LocalDateTime createdAt;

    public ProductionPlanItem() {}

    public ProductionPlanItem(int planItemID, int planID, int productID, int plannedQuantity, LocalDateTime createdAt) {
        this.planItemID = planItemID;
        this.planID = planID;
        this.productID = productID;
        this.plannedQuantity = plannedQuantity;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getPlanItemID() { return planItemID; }
    public void setPlanItemID(int planItemID) { this.planItemID = planItemID; }

    public int getPlanID() { return planID; }
    public void setPlanID(int planID) { this.planID = planID; }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public int getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(int plannedQuantity) { this.plannedQuantity = plannedQuantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
