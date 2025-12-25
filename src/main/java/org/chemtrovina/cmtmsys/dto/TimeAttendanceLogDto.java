package org.chemtrovina.cmtmsys.dto;

import lombok.Data;
import org.chemtrovina.cmtmsys.model.enums.AttendanceTimeStatus;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeAttendanceLogDto {

    private int logId;
    private int no;
    private int employeeId;

    private String mscnId1;
    private String input;           // NEW

    private String fullName;
    private String company;
    private String gender;
    private String departmentName;
    private String positionName;

    private String jobTitle;        // NEW
    private String managerName;     // NEW

    private LocalDate birthDate;
    private LocalDate entryDate;

    private String phoneNumber;     // NEW
    private String shiftCode;       // NEW
    private String shiftName;

    private String attendance;      // NEW → colX

    private String note;

    private String in;
    private String out;

    private AttendanceTimeStatus inStatus;   // ⭐
    private AttendanceTimeStatus outStatus;  // ⭐

    private String codeNow;
    private LocalDate scanDate;
    private LocalTime scanTime;

    // ====== GETTERS / SETTERS ======
}
