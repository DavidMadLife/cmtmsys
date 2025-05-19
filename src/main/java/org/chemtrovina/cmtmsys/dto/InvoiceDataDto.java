package org.chemtrovina.cmtmsys.dto;

public class InvoiceDataDto {
    private String sapCode;
    private int quantity;

    public InvoiceDataDto(String sapCode, int quantity) {
        this.sapCode = sapCode;
        this.quantity = quantity;
    }

    public InvoiceDataDto() {

    }

    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
