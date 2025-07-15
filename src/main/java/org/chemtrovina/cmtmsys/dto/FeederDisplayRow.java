package org.chemtrovina.cmtmsys.dto;

import org.chemtrovina.cmtmsys.model.Feeder;

public class FeederDisplayRow {
    private String feederCode;
    private String machine;
    private String sapCode;
    private int feederQty;
    private int feederId;

    private String rollCode = "";
    private Integer materialQty = 0;
    private String status = "Chưa gắn"; // mặc định ban đầu

    public static FeederDisplayRow fromFeeder(Feeder feeder) {
        FeederDisplayRow row = new FeederDisplayRow();
        row.feederId = feeder.getFeederId();
        row.feederCode = feeder.getFeederCode();
        row.machine = feeder.getMachine();
        row.sapCode = feeder.getSapCode();
        row.feederQty = feeder.getQty();
        row.rollCode = "";
        row.materialQty = 0;
        row.status = "Chưa gắn";
        return row;
    }

    // Getters và Setters cho TableView binding

    public String getFeederCode() {
        return feederCode;
    }
    public void setFeederCode(String feederCode) {
        this.feederCode = feederCode;
    }

    public String getMachine() {
        return machine;
    }
    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }

    public int getFeederQty() {
        return feederQty;
    }
    public void setFeederQty(int feederQty) {
        this.feederQty = feederQty;
    }

    public int getFeederId() {
        return feederId;
    }
    public void setFeederId(int feederId) {
        this.feederId = feederId;
    }

    public String getRollCode() {
        return rollCode;
    }
    public void setRollCode(String rollCode) {
        this.rollCode = rollCode;
    }

    public Integer getMaterialQty() {
        return materialQty;
    }
    public void setMaterialQty(Integer materialQty) {
        this.materialQty = materialQty;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
