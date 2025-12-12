package org.chemtrovina.cmtmsys.model;

public class ShiftTypeEmployee {

    private String shiftCode;      // e.g. A, B, C, OT, OFF
    private String shiftName;      // e.g. Ca A, Ca B
    private String description;    // mô tả ca

    public ShiftTypeEmployee() {}

    public ShiftTypeEmployee(String shiftCode, String shiftName, String description) {
        this.shiftCode = shiftCode;
        this.shiftName = shiftName;
        this.description = description;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftName() {
        return shiftName;
    }
    
    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
