package org.chemtrovina.cmtmsys.dto;

public class HistoryDetailViewDto {
    private int id;
    private String makerCode;
    private String maker;
    private String sapCode;
    private int moq;
    private int qty;
    private int reelQty;
    private Boolean invoice;
    public HistoryDetailViewDto() {

    }

    public HistoryDetailViewDto(int id, String makerCode, String sapCode, String maker, int moq, int qty, int reelQty, Boolean invoice) {
        this.id = id;
        this.makerCode = makerCode;
        this.maker = maker;
        this.sapCode = sapCode;
        this.moq = moq;
        this.qty = qty;
        this.reelQty = reelQty;
        this.invoice = invoice;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getMakerCode() {
        return makerCode;
    }
    public void setMakerCode(String makerCode) {
        this.makerCode = makerCode;
    }
    public String getMaker() {
        return maker;
    }
    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
    public int getMoq() {
        return moq;
    }
    public void setMoq(int moq) {
        this.moq = moq;
    }
    public int getQty() {
        return qty;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public int getReelQty() {
        return reelQty;
    }
    public void setReelQty(int reelQty) {
        this.reelQty = reelQty;
    }
    public Boolean getInvoice() {
        return invoice;
    }
    public void setInvoice(Boolean invoice) {
        this.invoice = invoice;
    }

}
