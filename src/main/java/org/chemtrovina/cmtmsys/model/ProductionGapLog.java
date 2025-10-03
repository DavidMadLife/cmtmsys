package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ProductionGapLog {
    private long gapId;
    private int productId;
    private int warehouseId;
    private int prevLogId;
    private int currLogId;
    private int pidDistanceSec;
    private String status;            // "TOR" / "POR" / "IDLE"
    private String reason;            // VD: "Model Change"
    private LocalDateTime createdAt;

    public long getGapId() {
        return gapId;
    }
    public void setGapId(long gapId) {
        this.gapId = gapId;
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

    public int getPrevLogId() {
        return prevLogId;
    }
    public void setPrevLogId(int prevLogId) {
        this.prevLogId = prevLogId;
    }

    public int getCurrLogId() {
        return currLogId;
    }
    public void setCurrLogId(int currLogId) {
        this.currLogId = currLogId;
    }

    public int getPidDistanceSec() {
        return pidDistanceSec;
    }
    public void setPidDistanceSec(int pidDistanceSec) {
        this.pidDistanceSec = pidDistanceSec;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
