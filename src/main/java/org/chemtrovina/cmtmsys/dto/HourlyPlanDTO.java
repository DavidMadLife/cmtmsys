package org.chemtrovina.cmtmsys.dto;

public class HourlyPlanDTO {
    private int slotIndex;
    private int planQuantity;

    public HourlyPlanDTO(int slotIndex, int planQuantity) {
        this.slotIndex = slotIndex;
        this.planQuantity = planQuantity;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getPlanQuantity() {
        return planQuantity;
    }
}
