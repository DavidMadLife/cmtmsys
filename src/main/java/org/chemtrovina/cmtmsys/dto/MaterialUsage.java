package org.chemtrovina.cmtmsys.dto;


import java.time.LocalDateTime;

public class MaterialUsage {
    private String sapCode;
    private String rollCode;
    private int quantityUsed;
    private String spec;
    private String lot;
    private String maker;
    private LocalDateTime created;
    private String warehouseName;

    public MaterialUsage(String sapCode, String rollCode, int quantityUsed, String warehouseName, String spec, String lot, String maker, LocalDateTime created) {
        this.sapCode = sapCode;
        this.rollCode = rollCode;
        this.quantityUsed = quantityUsed;
        this.spec = spec;
        this.lot = lot;
        this.maker = maker;
        this.created = created;
        this.warehouseName = warehouseName;
    }

    // Getters
    public String getSapCode() { return sapCode; }
    public String getRollCode() { return rollCode; }
    public int getQuantityUsed() { return quantityUsed; }
    public String getSpec() { return spec; }
    public String getLot() { return lot; }
    public LocalDateTime getCreated() { return created; }
    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getMaker() { return maker; }
    public void setMaker(String maker) { this.maker = maker; }
}

