package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class FeederAssignmentMaterial {
    private int id;
    private int assignmentId;
    private int materialId;
    private boolean isSupplement;
    private LocalDateTime attachedAt;
    private LocalDateTime detachedAt;
    private boolean isActive;
    private String note;

    public FeederAssignmentMaterial() {
        this.attachedAt = LocalDateTime.now();
        this.isActive = true;
        this.isSupplement = false;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }

    public boolean isSupplement() { return isSupplement; }
    public void setSupplement(boolean isSupplement) { this.isSupplement = isSupplement; }

    public LocalDateTime getAttachedAt() { return attachedAt; }
    public void setAttachedAt(LocalDateTime attachedAt) { this.attachedAt = attachedAt; }

    public LocalDateTime getDetachedAt() { return detachedAt; }
    public void setDetachedAt(LocalDateTime detachedAt) { this.detachedAt = detachedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
