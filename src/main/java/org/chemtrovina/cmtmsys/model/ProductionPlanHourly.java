// ✅ Model: ProductionPlanHourly.java
package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class ProductionPlanHourly {
    private int hourlyId; // ID tự tăng
    private int dailyId; // FK tới ProductionPlanDaily
    private int slotIndex; // Slot 0..11
    private LocalDateTime runHour; // Thời điểm bắt đầu slot
    private int planQuantity;
    private int actualQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getHourlyId() {
        return hourlyId;
    }

    public void setHourlyId(int hourlyId) {
        this.hourlyId = hourlyId;
    }

    public int getDailyId() {
        return dailyId;
    }

    public void setDailyId(int dailyId) {
        this.dailyId = dailyId;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public LocalDateTime getRunHour() {
        return runHour;
    }

    public void setRunHour(LocalDateTime runHour) {
        this.runHour = runHour;
    }

    public int getPlanQuantity() {
        return planQuantity;
    }

    public void setPlanQuantity(int planQuantity) {
        this.planQuantity = planQuantity;
    }

    public int getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
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
}
