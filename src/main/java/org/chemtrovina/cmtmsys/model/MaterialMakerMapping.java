package org.chemtrovina.cmtmsys.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialMakerMapping {
    private int id;
    private String rollCode;
    private String spec;
    private String makerPN;
    private String maker;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaterialMakerMapping() {}

    public MaterialMakerMapping(int id, String rollCode, String spec, String makerPN, String maker,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.rollCode = rollCode;
        this.spec = spec;
        this.makerPN = makerPN;
        this.maker = maker;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters...
}
