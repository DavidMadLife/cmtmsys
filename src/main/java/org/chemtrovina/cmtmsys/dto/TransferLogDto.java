package org.chemtrovina.cmtmsys.dto;

public class TransferLogDto {
    private String barcode;
    private String fromWarehouse;
    private String toWarehouse;
    private String formattedTime;
    private String employeeId;
    private String spec;
    private String sapCode;

    // constructor, getter/setter
    public TransferLogDto() {

    }
    public TransferLogDto(String barcode, String fromWarehouse, String toWarehouse, String formattedTime,  String employeeId, String spec, String sapCode) {
        this.barcode = barcode;
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;
        this.formattedTime = formattedTime;
        this.employeeId = employeeId;
        this.spec = spec;
        this.sapCode = sapCode;
    }
    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    public String getFromWarehouse() {
        return fromWarehouse;
    }
    public void setFromWarehouse(String fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }
    public String getToWarehouse() {
        return toWarehouse;
    }
    public void setToWarehouse(String toWarehouse) {
        this.toWarehouse = toWarehouse;
    }
    public String getFormattedTime() {
        return formattedTime;
    }
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    public String getSpec() {
        return spec;
    }
    public void setSpec(String spec) {
        this.spec = spec;
    }
    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
}
