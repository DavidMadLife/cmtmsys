package org.chemtrovina.cmtmsys.model;

public class Feeder {
    private int feederId;
    private int modelLineId;
    private String feederCode;
    private String sapCode;
    private int qty;
    private String machine;

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public int getFeederId() {
        return feederId;
    }
    public void setFeederId(int feederId) {
        this.feederId = feederId;
    }

    public int getModelLineId() {
        return modelLineId;
    }
    public void setModelLineId(int modelLineId) {
        this.modelLineId = modelLineId;
    }

    public String getFeederCode() {
        return feederCode;
    }
    public void setFeederCode(String feederCode) {
        this.feederCode = feederCode;
    }

    public String getSapCode() {
        return sapCode;
    }
    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }

    public int getQty() {
        return qty;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
}
