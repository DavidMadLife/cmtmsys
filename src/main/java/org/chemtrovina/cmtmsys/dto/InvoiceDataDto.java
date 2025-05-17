package org.chemtrovina.cmtmsys.dto;

public class InvoiceDataDto {
    private String invoiceNo;
    private int row;

    public InvoiceDataDto(String invoiceNo, int row) {
        this.invoiceNo = invoiceNo;
        this.row = row;
    }

    public InvoiceDataDto() {

    }

    public String getInvoiceNo() {
        return invoiceNo;
    }
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
}
