package org.chemtrovina.cmtmsys.dto;

public class HistorySummary {
    private String sapPN;
    private int totalScanned;
    private String makerPN;
    private String maker;
    private String spec;

    public HistorySummary(String sapPN, int totalScanned, String makerPN, String maker, String spec) {
        this.sapPN = sapPN;
        this.totalScanned = totalScanned;
        this.makerPN = makerPN;
        this.maker = maker;
        this.spec = spec;
    }

    public HistorySummary() {}

    public String getSapPN() {
        return sapPN;
    }

    public void setSapPN(String sapPN) {
        this.sapPN = sapPN;
    }

    public int getTotalScanned() {
        return totalScanned;
    }

    public void setTotalScanned(int totalScanned) {
        this.totalScanned = totalScanned;
    }

    public String getMakerPN() {
        return makerPN;
    }

    public void setMakerPN(String makerPN) {
        this.makerPN = makerPN;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }
}
