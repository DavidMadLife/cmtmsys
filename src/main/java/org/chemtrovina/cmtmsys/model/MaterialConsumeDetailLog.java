package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class MaterialConsumeDetailLog {

    private int logId;
    private int planItemId;
    private LocalDate runDate;
    private int materialId;
    private int consumedQty;
    private LocalDateTime createdAt;
    private Integer sourceLogId;

    // Constructors
    public MaterialConsumeDetailLog() {
    }

    public MaterialConsumeDetailLog(int planItemId, LocalDate runDate, int materialId, int consumedQty, LocalDateTime createdAt, Integer sourceLogId) {
        this.planItemId = planItemId;
        this.runDate = runDate;
        this.materialId = materialId;
        this.consumedQty = consumedQty;
        this.createdAt = createdAt;
        this.sourceLogId = sourceLogId;
    }

    // Getters and Setters

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(Integer planItemId) {
        this.planItemId = (planItemId == null) ? 0 : planItemId;
    }


    public LocalDate getRunDate() {
        return runDate;
    }

    public void setRunDate(LocalDate runDate) {
        this.runDate = runDate;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public int getConsumedQty() {
        return consumedQty;
    }

    public void setConsumedQty(int consumedQty) {
        this.consumedQty = consumedQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getSourceLogId() {
        return sourceLogId;
    }

    public void setSourceLogId(Integer sourceLogId) {
        this.sourceLogId = sourceLogId;
    }

}
