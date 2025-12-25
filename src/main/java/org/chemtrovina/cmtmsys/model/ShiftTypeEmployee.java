package org.chemtrovina.cmtmsys.model;

import lombok.Data;

import java.time.LocalTime;


@Data
public class ShiftTypeEmployee {

    private String shiftCode;
    private String shiftName;
    private String description;

    private LocalTime startTime;
    private LocalTime endTime;

    private Boolean isOvernight; // ⚠️ tên field
}

