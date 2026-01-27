package org.chemtrovina.cmtmsys.model.enums;

public enum AttendanceTimeStatus {
    OK,
    LATE,
    EARLY,
    TOO_EARLY_IN,   // IN sớm hơn start - 30p
    TOO_LATE_OUT    // OUT trễ hơn end + 30p
}

