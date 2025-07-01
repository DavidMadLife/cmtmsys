package org.chemtrovina.cmtmsys.dto;

public class MaterialRequirementDto {
    private String workOrderCode;
    private String productCode;
    private int productQty;
    private String sappn;
    private int bomPerUnit;
    private int requiredQty;

    public MaterialRequirementDto() {}

    public MaterialRequirementDto(String workOrderCode, String productCode, int productQty,
                                  String sappn, int bomPerUnit, int requiredQty) {
        this.workOrderCode = workOrderCode;
        this.productCode = productCode;
        this.productQty = productQty;
        this.sappn = sappn;
        this.bomPerUnit = bomPerUnit;
        this.requiredQty = requiredQty;
    }

    public String getWorkOrderCode() {
        return workOrderCode;
    }

    public void setWorkOrderCode(String workOrderCode) {
        this.workOrderCode = workOrderCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getProductQty() {
        return productQty;
    }

    public void setProductQty(int productQty) {
        this.productQty = productQty;
    }

    public String getSappn() {
        return sappn;
    }

    public void setSappn(String sappn) {
        this.sappn = sappn;
    }

    public int getBomPerUnit() {
        return bomPerUnit;
    }

    public void setBomPerUnit(int bomPerUnit) {
        this.bomPerUnit = bomPerUnit;
    }

    public int getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(int requiredQty) {
        this.requiredQty = requiredQty;
    }

}


