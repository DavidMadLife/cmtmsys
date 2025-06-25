package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ProductBOM {
    private int bomId;
    private int productId;
    private String sappn;
    private double quantity;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public int getBomId() {
        return bomId;
    }
    public void setBomId(int bomId) {
        this.bomId = bomId;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getSappn() {
        return sappn;
    }
    public void setSappn(String sappn) {
        this.sappn = sappn;
    }

    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
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
