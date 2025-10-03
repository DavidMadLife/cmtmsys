package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ShiftSummary {
    private long summaryId;
    private int shiftId;
    private int warehouseId;

    private int totalTimeSec;
    private int torTimeSec;
    private int porTimeSec;
    private int idleTimeSec;

    private int torQty;
    private int porQty;
    private int idleQty;

    private double torPercent;
    private double porPercent;
    private double idlePercent;

    private int mcTimeSec;
    private int mcQty;
    private double mcPercent;

    private LocalDateTime createdAt;

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

    public long getSummaryId() {
        return summaryId;
    }
    public void setSummaryId(long summaryId) {
        this.summaryId = summaryId;
    }

    public int getShiftId() {
        return shiftId;
    }
    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }
    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }


    public int getTotalTimeSec() {
        return totalTimeSec;
    }
    public void setTotalTimeSec(int totalTimeSec) {
        this.totalTimeSec = totalTimeSec;
    }

    public int getTorTimeSec() {
        return torTimeSec;
    }
    public void setTorTimeSec(int torTimeSec) {
        this.torTimeSec = torTimeSec;
    }

    public int getPorTimeSec() {
        return porTimeSec;
    }
    public void setPorTimeSec(int porTimeSec) {
        this.porTimeSec = porTimeSec;
    }

    public int getIdleTimeSec() {
        return idleTimeSec;
    }
    public void setIdleTimeSec(int idleTimeSec) {
        this.idleTimeSec = idleTimeSec;
    }

    public int getTorQty() {
        return torQty;
    }
    public void setTorQty(int torQty) {
        this.torQty = torQty;
    }

    public int getPorQty() {
        return porQty;
    }
    public void setPorQty(int porQty) {
        this.porQty = porQty;
    }

    public int getIdleQty() {
        return idleQty;
    }
    public void setIdleQty(int idleQty) {
        this.idleQty = idleQty;
    }

    public double getTorPercent() {
        return torPercent;
    }
    public void setTorPercent(double torPercent) {
        this.torPercent = torPercent;
    }

    public double getPorPercent() {
        return porPercent;
    }
    public void setPorPercent(double porPercent) {
        this.porPercent = porPercent;
    }

    public double getIdlePercent() {
        return idlePercent;
    }
    public void setIdlePercent(double idlePercent) {
        this.idlePercent = idlePercent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}
