package org.chemtrovina.cmtmsys.model;

public class ShiftChem {
    private int shiftId;
    private String shiftName;

    public ShiftChem() {

    }
    public ShiftChem(int shiftId, String shiftName) {
        this.shiftId = shiftId;
        this.shiftName = shiftName;
    }
    public int getShiftId() {
        return shiftId;
    }
    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }
    public String getShiftName() {
        return shiftName;
    }
    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "shiftId=" + shiftId +
                "shiftName=" + shiftName +
                '}';

    }
}
