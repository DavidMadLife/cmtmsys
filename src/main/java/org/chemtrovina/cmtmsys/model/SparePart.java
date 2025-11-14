package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;

public class SparePart {
    private int id;                     // Khóa chính
    private LocalDate date;             // Ngày nhập hoặc ghi nhận

    private String name;                // Tên dụng cụ/phụ tùng
    private String code;                // Mã dụng cụ/phụ tùng
    private byte[] imageData;           // Ảnh lưu trong DB (BLOB)
    private String supplier;            // Nhà cung cấp
    private String manufacturer;        // Nhà sản xuất
    private int quantity;               // Số lượng
    private String unit;                // Đơn vị tính (EA, PCS, ...)
    private String warehouseKeeper;     // Người nhập kho (KT)
    private String note;                // Ghi chú (optional)

    public SparePart() {
    }

    public SparePart(int id, LocalDate date, String name, String code, byte[] imageData,
                     String supplier, String manufacturer, int quantity,
                     String unit, String warehouseKeeper, String note) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.code = code;
        this.imageData = imageData;
        this.supplier = supplier;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.unit = unit;
        this.warehouseKeeper = warehouseKeeper;
        this.note = note;
    }

    // ==== Getters & Setters ====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getWarehouseKeeper() { return warehouseKeeper; }
    public void setWarehouseKeeper(String warehouseKeeper) { this.warehouseKeeper = warehouseKeeper; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
