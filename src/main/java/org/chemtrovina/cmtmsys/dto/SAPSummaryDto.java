package org.chemtrovina.cmtmsys.dto;

public class SAPSummaryDto {
    private String sapCode;
    private int required;
    private int scanned;
    private String status;

    public SAPSummaryDto(String sapCode, int required) {
        this.sapCode = sapCode;
        this.required = required;
        this.scanned = 0;
        this.status = "Thiếu";
    }

    public String getSapCode() { return sapCode; }
    public int getRequired() { return required; }
    public int getScanned() { return scanned; }   // tên phải đúng: getScanned
    public String getStatus() { return status; }
    public void setSapCode(String sapCode) { this.sapCode = sapCode; }
    public void setRequired(int required) { this.required = required; }
    public void setScanned(int scanned) { this.scanned = scanned; }
    public void setStatus(String status) { this.status = status; }

    public void incrementScanned(int qty) {
        this.scanned += qty;
        this.status = (scanned >= required) ? "Đủ" : "Thiếu";
    }
}

