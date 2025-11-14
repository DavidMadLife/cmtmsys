package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class SparePartOutput {
    private int id;
    private int sparePartId;
    private int quantity;
    private String issuer;
    private String receiver;
    private String line;
    private String note;
    private LocalDateTime outputDate;

    // Dữ liệu join
    private String sparePartName;
    private String sparePartCode;
    private String model;
    private String serial;
    private byte[] imageData;

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSparePartId() { return sparePartId; }
    public void setSparePartId(int sparePartId) { this.sparePartId = sparePartId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getOutputDate() { return outputDate; }
    public void setOutputDate(LocalDateTime outputDate) { this.outputDate = outputDate; }

    public String getSparePartName() { return sparePartName; }
    public void setSparePartName(String sparePartName) { this.sparePartName = sparePartName; }
    public String getSparePartCode() { return sparePartCode; }
    public void setSparePartCode(String sparePartCode) { this.sparePartCode = sparePartCode; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
}
