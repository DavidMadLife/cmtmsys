package org.chemtrovina.cmtmsys.dto;

public class ShiftSummaryDTO {
    private String warehouseName;
    private String startTime;
    private String endTime;

    private int porTimeSec;
    private int porQty;
    private double porPercent;

    private int torTimeSec;
    private int torQty;
    private double torPercent;

    private String idleStart;
    private int idleQty;
    private int idleTimeSec;

    private String mcStart;
    private int mcQty;

    // === GETTER & SETTER ===

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPorTimeSec() {
        return porTimeSec;
    }

    public void setPorTimeSec(int porTimeSec) {
        this.porTimeSec = porTimeSec;
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

    public int getTorTimeSec() {
        return torTimeSec;
    }

    public void setTorTimeSec(int torTimeSec) {
        this.torTimeSec = torTimeSec;
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

    public int getIdleTimeSec() {
        return idleTimeSec;
    }

    public void setIdleTimeSec(int idleTimeSec) {
        this.idleTimeSec = idleTimeSec;
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
