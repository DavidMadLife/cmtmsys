package org.chemtrovina.cmtmsys.dto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ShiftScheduleDto {
    private int no;
    private String MSCNID1;
    private String fullName;
    private Map<LocalDate, String> shiftMap = new HashMap<>();

    public ShiftScheduleDto() {

    }

    // Constructor
    public ShiftScheduleDto(int no, String MSCNID1, String fullName) {
        this.no = no;
        this.MSCNID1 = MSCNID1;
        this.fullName = fullName;
    }

    // Getters, Setters
    public String getShiftForDate(LocalDate date) {
        return shiftMap.getOrDefault(date, "");
    }

    public void setShift(LocalDate date, String shiftLabel) {
        shiftMap.put(date, shiftLabel);
    }

    public int getNo() {
        return no;
    }
    public void setNo(int no) {
        this.no = no;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getMSCNID1() {
        return MSCNID1;
    }
    public void setMSCNID1(String MSCNID1) {
        this.MSCNID1 = MSCNID1;
    }

}
