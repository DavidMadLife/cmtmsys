package org.chemtrovina.cmtmsys.model;

public class EBoardProduct {
    private int id;
    private int setId;               // FK -> EBoardSet
    private int productId;           // FK -> Product
    private String circuitType;      // LED / PD
    private String description;      // VD: CTSO_850_LED_B1, CTSO_850_PD_T1

    public EBoardProduct() {}

    public EBoardProduct(int id, int setId, int productId, String circuitType, String description) {
        this.id = id;
        this.setId = setId;
        this.productId = productId;
        this.circuitType = circuitType;
        this.description = description;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getSetId() {
        return setId;
    }
    public void setSetId(int setId) {
        this.setId = setId;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getCircuitType() {
        return circuitType;
    }
    public void setCircuitType(String circuitType) {
        this.circuitType = circuitType;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
