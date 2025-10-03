package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class StencilTransfer {
    private int transferId;
    private int stencilId;
    private String barcode;
    private Integer fromWarehouseId;
    private Integer toWarehouseId;
    private LocalDateTime transferDate;
    private String performedBy;
    private String note;

    public StencilTransfer() {}

    public StencilTransfer(int transferId, int stencilId, String barcode,
                           Integer fromWarehouseId, Integer toWarehouseId,
                           LocalDateTime transferDate, String performedBy, String note) {
        this.transferId = transferId;
        this.stencilId = stencilId;
        this.barcode = barcode;
        this.fromWarehouseId = fromWarehouseId;
        this.toWarehouseId = toWarehouseId;
        this.transferDate = transferDate;
        this.performedBy = performedBy;
        this.note = note;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

    public int getStencilId() {
        return stencilId;
    }

    public void setStencilId(int stencilId) {
        this.stencilId = stencilId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getFromWarehouseId() {
        return fromWarehouseId;
    }

    public void setFromWarehouseId(Integer fromWarehouseId) {
        this.fromWarehouseId = fromWarehouseId;
    }

    public Integer getToWarehouseId() {
        return toWarehouseId;
    }

    public void setToWarehouseId(Integer toWarehouseId) {
        this.toWarehouseId = toWarehouseId;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
