package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDateTime;

public class ProductBomDto {

    private String productCode;
    private String sappn;
    private double quantity;
    private String modelType;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ProductBomDto(String productCode, String sappn, double quantity, String modelType,
                         LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.productCode = productCode;
        this.sappn = sappn;
        this.modelType = modelType;
        this.quantity = quantity;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getModelType() {return modelType;}

    public String getProductCode() { return productCode; }
    public String getSappn() { return sappn; }
    public double getQuantity() { return quantity; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
}
