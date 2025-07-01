package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class WorkOrder {
    private int workOrderId;
    private String workOrderCode;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public WorkOrder() {}

    public WorkOrder(int workOrderId, String workOrderCode, String description, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.workOrderId = workOrderId;
        this.workOrderCode = workOrderCode;
        this.description = description;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public int getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(int workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
