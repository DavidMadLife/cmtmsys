package org.chemtrovina.cmtmsys.dto;

public class TransferredDto {
    private String rollCode;
    private String sapCode;
    private String spec;
    private int quantity;
    private String fromWarehouse;
    private String toWarehouse;

    // Constructor + Getter/Setter
    public TransferredDto(String rollCode, String sapCode, String spec, int quantity, String fromWarehouse, String toWarehouse) {
        this.rollCode = rollCode;
        this.sapCode = sapCode;
        this.spec = spec;
        this.quantity = quantity;
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;

    }
    public String getRollCode() {
        return rollCode;
    }
    public void setRollCode(String rollCode) {
        this.rollCode = rollCode;
    }
    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
    public String getSpec() {
        return spec;
    }
    public void setSpec(String spec) {
        this.spec = spec;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getFromWarehouse() {
        return fromWarehouse;
    }
    public void setFromWarehouse(String fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }
    public String getToWarehouse() {
        return toWarehouse;
    }
    public void setToWarehouse(String toWarehouse) {
        this.toWarehouse = toWarehouse;
    }
}

