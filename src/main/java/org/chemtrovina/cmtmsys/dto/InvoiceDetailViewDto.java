package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDate;

public class InvoiceDetailViewDto {
    private int invoiceId;
    private LocalDate invoiceDate;
    private String invoiceNo;
    private String sapCode;
    private int quantity;
    private int moq;
    private int reelQty;

    public InvoiceDetailViewDto() {
    }

    public InvoiceDetailViewDto( int invoiceId ,LocalDate invoiceDate, String invoiceNo, String sapCode, int quantity, int moq, int reelQty) {
        this.invoiceId = invoiceId;
        this.invoiceDate = invoiceDate;
        this.invoiceNo = invoiceNo;
        this.sapCode = sapCode;
        this.quantity = quantity;
        this.moq = moq;
        this.reelQty = reelQty;
    }

    public int getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
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

    public int getMoq() {
        return moq;
    }

    public void setMoq(int moq) {
        this.moq = moq;
    }

    public int getReelQty() {
        return reelQty;
    }

    public void setReelQty(int reelQty) {
        this.reelQty = reelQty;
    }
}
