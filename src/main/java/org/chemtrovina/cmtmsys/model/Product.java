package org.chemtrovina.cmtmsys.model;

import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productCode;
    private String name;
    private String description;
    private ModelType modelType;  // Thêm dòng này
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public ModelType getModelType() {
        return modelType;
    }
    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
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
