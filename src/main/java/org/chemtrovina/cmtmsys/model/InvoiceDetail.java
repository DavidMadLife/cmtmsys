package org.chemtrovina.cmtmsys.model;

public class InvoiceDetail {
    private int id;
    private int invoiceId;
    private String sapPN;
    private int quantity;
    private int moq;
    private String status;
    private int totalReel;

    public InvoiceDetail() {
    }

    public InvoiceDetail(int id, int invoiceId, String sapPN, int quantity, int moq, String status, int totalReel) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.sapPN = sapPN;
        this.quantity = quantity;
        this.moq = moq;
        this.status = status;
        this.totalReel = totalReel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getSapPN() {
        return sapPN;
    }

    public void setSapPN(String sapPN) {
        this.sapPN = sapPN;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalReel() {
        return totalReel;
    }

    public void setTotalReel(int totalReel) {
        this.totalReel = totalReel;
    }

    @Override
    public String toString() {
        return "InvoiceDetail{" +
                "id=" + id +
                ", invoiceId='" + invoiceId + '\'' +
                ", sapPN='" + sapPN + '\'' +
                ", quantity=" + quantity +
                ", moq=" + moq +
                ", status='" + status + '\'' +
                ", totalReel=" + totalReel +
                '}';
    }
}
