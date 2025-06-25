package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class ProductBomDto {
    private String productCode;
    private String sappn;
    private double quantity;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ProductBomDto(String productCode, String sappn, double quantity,
                         LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.productCode = productCode;
        this.sappn = sappn;
        this.quantity = quantity;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getProductCode() { return productCode; }
    public String getSappn() { return sappn; }
    public double getQuantity() { return quantity; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
}
