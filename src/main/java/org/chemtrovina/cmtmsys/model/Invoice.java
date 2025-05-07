package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;

public class Invoice {
    private int id;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private LocalDate createdAt;
    private String status;


    public Invoice(){

    }
    public Invoice(int id, String invoiceNo, LocalDate invoiceDate, LocalDate createdAt, String status){
        this.id = id;
        this.invoiceNo = invoiceNo;
        this.invoiceDate = invoiceDate;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }

}
