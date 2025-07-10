package org.chemtrovina.cmtmsys.model;

public class ModelLine {
    private int modelLineId;
    private int productId;
    private int warehouseId;

    public int getModelLineId() {
        return modelLineId;
    }
    public void setModelLineId(int modelLineId) {
        this.modelLineId = modelLineId;
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
}
