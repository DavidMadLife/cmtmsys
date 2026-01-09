package org.chemtrovina.cmtmsys.dto;

import lombok.Data;

@Data
public class AttendanceSummaryDto {

    // CA NGÀY
    private int dayTotal;
    private int dayPresent;
    private int dayAbsent;

    // CA ĐÊM
    private int nightTotal;
    private int nightPresent;
    private int nightAbsent;

    // CHƯA CÓ CA
    private int naTotal;
    private int naPresent;
    private int naAbsent;
}
