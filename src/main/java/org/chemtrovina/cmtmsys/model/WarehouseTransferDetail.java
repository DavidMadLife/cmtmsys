package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class WarehouseTransferDetail {
    private int transferDetailId;
    private int transferId;
    private String rollCode;
    private String sapCode;
    private int quantity;
    private LocalDateTime createdAt;

    public WarehouseTransferDetail() {}

    public WarehouseTransferDetail(int transferDetailId, int transferId, String rollCode,
                                   String sapCode, int quantity, LocalDateTime createdAt) {
        this.transferDetailId = transferDetailId;
        this.transferId = transferId;
        this.rollCode = rollCode;
        this.sapCode = sapCode;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    // Getters and setters

    public int getTransferDetailId() { return transferDetailId; }
    public void setTransferDetailId(int transferDetailId) { this.transferDetailId = transferDetailId; }

    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public String getRollCode() { return rollCode; }
    public void setRollCode(String rollCode) { this.rollCode = rollCode; }

    public String getSapCode() { return sapCode; }
    public void setSapCode(String sapCode) { this.sapCode = sapCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "WarehouseTransferDetail{" +
                "transferDetailId=" + transferDetailId +
                ", transferId=" + transferId +
                ", rollCode='" + rollCode + '\'' +
                ", sapCode='" + sapCode + '\'' +
                ", quantity=" + quantity +
                ", createdAt=" + createdAt +
                '}';
    }
}
