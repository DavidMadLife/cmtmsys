package org.chemtrovina.cmtmsys.dto;

public class ShiftSummaryDTO {
    private String warehouseName;
    private String startTime;
    private String endTime;

    // --- POR ---
    private int porTimeSec;
    private int porQty;
    private double porPercent;

    // --- TOR ---
    private int torTimeSec;
    private int torQty;
    private double torPercent;

    // --- IDLE ---
    private int idleTimeSec;
    private int idleQty;
    private double idlePercent;

    // --- M/C ---
    private int mcTimeSec;
    private int mcQty;
    private double mcPercent;

    // === GETTERS & SETTERS ===
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

    public int getIdleTimeSec() {
        return idleTimeSec;
    }

    public void setIdleTimeSec(int idleTimeSec) {
        this.idleTimeSec = idleTimeSec;
    }

    public int getIdleQty() {
        return idleQty;
    }

    public void setIdleQty(int idleQty) {
        this.idleQty = idleQty;
    }

    public double getIdlePercent() {
        return idlePercent;
    }

    public void setIdlePercent(double idlePercent) {
        this.idlePercent = idlePercent;
    }

    public int getMcTimeSec() {
        return mcTimeSec;
    }

    public void setMcTimeSec(int mcTimeSec) {
        this.mcTimeSec = mcTimeSec;
    }

    public int getMcQty() {
        return mcQty;
    }

    public void setMcQty(int mcQty) {
        this.mcQty = mcQty;
    }

    public double getMcPercent() {
        return mcPercent;
    }

    public void setMcPercent(double mcPercent) {
        this.mcPercent = mcPercent;
    }
}
