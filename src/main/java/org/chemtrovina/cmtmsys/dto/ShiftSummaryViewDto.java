package org.chemtrovina.cmtmsys.dto;

public class ShiftSummaryViewDto {
    private String warehouseName;
    private String shiftStart;
    private String shiftEnd;

    private int porTime;
    private int porQty;
    private double porPercent;

    private int torTime;
    private int torQty;
    private double torPercent;

    private String idleStart;
    private int idleQty;
    private int idleTime;

    private String mcStart;
    private int mcQty;

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(String shiftStart) {
        this.shiftStart = shiftStart;
    }

    public String getShiftEnd() {
        return shiftEnd;
    }

    public void setShiftEnd(String shiftEnd) {
        this.shiftEnd = shiftEnd;
    }

    public int getPorTime() {
        return porTime;
    }

    public void setPorTime(int porTime) {
        this.porTime = porTime;
    }

    public int getPorQty() {
        return porQty;
    }

    public void setPorQty(int porQty) {
        this.porQty = porQty;
    }

    public double getPorPercent() {
        return porPercent;
    }

    public void setPorPercent(double porPercent) {
        this.porPercent = porPercent;
    }

    public int getTorTime() {
        return torTime;
    }

    public void setTorTime(int torTime) {
        this.torTime = torTime;
    }

    public int getTorQty() {
        return torQty;
    }

    public void setTorQty(int torQty) {
        this.torQty = torQty;
    }

    public double getTorPercent() {
        return torPercent;
    }

    public void setTorPercent(double torPercent) {
        this.torPercent = torPercent;
    }

    public String getIdleStart() {
        return idleStart;
    }

    public void setIdleStart(String idleStart) {
        this.idleStart = idleStart;
    }

    public int getIdleQty() {
        return idleQty;
    }

    public void setIdleQty(int idleQty) {
        this.idleQty = idleQty;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public String getMcStart() {
        return mcStart;
    }

    public void setMcStart(String mcStart) {
        this.mcStart = mcStart;
    }

    public int getMcQty() {
        return mcQty;
    }

    public void setMcQty(int mcQty) {
        this.mcQty = mcQty;
    }
}
