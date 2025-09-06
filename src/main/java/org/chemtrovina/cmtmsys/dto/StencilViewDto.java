package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDate;

public class StencilViewDto {
    private int stencilId;
    private String barcode;
    private String stencilNo;
    private int productId;
    private String productCode;
    private String productName;
    private String versionLabel;
    private String size;
    private int arrayCount;
    private LocalDate receivedDate;
    private String status;
    private String warehouse;
    private String note;
    private String modelType;

    // ===== Constructors =====
    public StencilViewDto() {}

    public StencilViewDto(int stencilId, String barcode, String stencilNo,
                          int productId, String productCode, String productName,
                          String versionLabel, String size, int arrayCount,
                          LocalDate receivedDate, String status,
                          String warehouse, String note, String modelType) {
        this.stencilId = stencilId;
        this.barcode = barcode;
        this.stencilNo = stencilNo;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.versionLabel = versionLabel;
        this.size = size;
        this.arrayCount = arrayCount;
        this.receivedDate = receivedDate;
        this.status = status;
        this.warehouse = warehouse;
        this.note = note;
        this.modelType = modelType;
    }

    // ===== Getter/Setter =====
    public int getStencilId() { return stencilId; }
    public void setStencilId(int stencilId) { this.stencilId = stencilId; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStencilNo() { return stencilNo; }
    public void setStencilNo(String stencilNo) { this.stencilNo = stencilNo; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public int getArrayCount() { return arrayCount; }
    public void setArrayCount(int arrayCount) { this.arrayCount = arrayCount; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getModelType() { return modelType; }  // ✅ THÊM
    public void setModelType(String modelType) { this.modelType = modelType; }
}
