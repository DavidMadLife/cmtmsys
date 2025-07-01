package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class WorkOrderItem {
    private int itemId;
    private int workOrderId;
    private int productId;
    private int quantity;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public WorkOrderItem() {}

    public WorkOrderItem(int itemId, int workOrderId, int productId, int quantity, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.itemId = itemId;
        this.workOrderId = workOrderId;
        this.productId = productId;
        this.quantity = quantity;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(int workOrderId) {
        this.workOrderId = workOrderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
