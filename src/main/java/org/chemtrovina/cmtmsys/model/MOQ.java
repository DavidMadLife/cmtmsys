package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class MOQ {
    private int id;
    private String maker;
    private String makerPN;
    private String sapPN;
    private Integer moq;
    private String msql;
    private String spec;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MOQ() {
    }

    public MOQ(int id, String maker, String makerPN, String sapPN, Integer moq, String msql, String spec, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.maker = maker;
        this.makerPN = makerPN;
        this.sapPN = sapPN;
        this.moq = moq;
        this.msql = msql;
        this.spec = spec;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter và Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getMakerPN() {
        return makerPN;
    }

    public void setMakerPN(String makerPN) {
        this.makerPN = makerPN;
    }

    public String getSapPN() {
        return sapPN;
    }

    public void setSapPN(String sapPN) {
        this.sapPN = sapPN;
    }

    public Integer getMoq() {
        return moq;
    }

    public void setMoq(Integer moq) {
        this.moq = moq;
    }

    public String getMsql() {
        return msql;
    }

    public void setMsql(String msql) {
        this.msql = msql;
    }
    public String getSpec() {return spec;}
    public void setSpec(String spec) {this.spec = spec;}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return makerPN + " (MOQ: " + moq + ", MSQL: " + msql + ")";
    }
}

