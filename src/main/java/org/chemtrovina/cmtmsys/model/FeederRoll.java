package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class FeederRoll {
    private int feederRollId;
    private int feederId;
    private int materialId;
    private int runId;
    private LocalDateTime attachedAt;
    private LocalDateTime detachedAt;
    private boolean isActive;

    public FeederRoll() {
        this.attachedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public int getFeederRollId() {
        return feederRollId;
    }

    public void setFeederRollId(int feederRollId) {
        this.feederRollId = feederRollId;
    }

    public int getFeederId() {
        return feederId;
    }

    public void setFeederId(int feederId) {
        this.feederId = feederId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public LocalDateTime getAttachedAt() {
        return attachedAt;
    }

    public void setAttachedAt(LocalDateTime attachedAt) {
        this.attachedAt = attachedAt;
    }

    public LocalDateTime getDetachedAt() {
        return detachedAt;
    }

    public void setDetachedAt(LocalDateTime detachedAt) {
        this.detachedAt = detachedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
