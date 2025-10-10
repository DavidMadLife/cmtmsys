package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class EBoardSet {
    private int setId;
    private String setName;           // CTSO_850, CTSI_0650, ...
    private String description;       // E-Board Set cho dòng ...
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EBoardSet() {}

    public EBoardSet(int setId, String setName, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.setId = setId;
        this.setName = setName;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getSetId() {
        return setId;
    }
    public void setSetId(int setId) {
        this.setId = setId;
    }

    public String getSetName() {
        return setName;
    }
    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

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
        return setName;
    }
}
