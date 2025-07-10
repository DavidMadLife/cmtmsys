package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MaterialConsumeLog {
    private int consumeId;
    private int planItemId;
    private LocalDate runDate;
    private int consumedQty;
    private LocalDateTime createdAt;

    public MaterialConsumeLog(int consumeId, int planItemId, LocalDate runDate, int consumedQty, LocalDateTime createdAt) {
        this.consumeId = consumeId;
        this.planItemId = planItemId;
        this.runDate = runDate;
        this.consumedQty = consumedQty;
        this.createdAt = createdAt;
    }

    public MaterialConsumeLog(int planItemId, LocalDate runDate, int consumedQty) {
        this.planItemId = planItemId;
        this.runDate = runDate;
        this.consumedQty = consumedQty;
    }

    // Getters & setters
    public int getConsumeId() { return consumeId; }
    public int getPlanItemId() { return planItemId; }
    public LocalDate getRunDate() { return runDate; }
    public int getConsumedQty() { return consumedQty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
