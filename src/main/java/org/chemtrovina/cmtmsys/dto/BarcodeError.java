package org.chemtrovina.cmtmsys.dto;

public class BarcodeError {
    private String barcode;
    private String reason;

    public BarcodeError(String barcode, String reason) {
        this.barcode = barcode;
        this.reason = reason;
    }

    public String getBarcode() { return barcode; }
    public String getReason() { return reason; }
}
