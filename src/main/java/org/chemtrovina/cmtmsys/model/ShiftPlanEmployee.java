package org.chemtrovina.cmtmsys.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShiftPlanEmployee {

    private int shiftPlanId;       // PK
    private int employeeId;        // FK Employee
    private LocalDate shiftDate;   // ngày phân ca

    private String shiftCode;      // ca kế hoạch (A/B/C/OFF…)
    private String shiftActual;    // ca thực tế
    private String note;           // ghi chú (đổi ca, OT…)

    private String importedBy;
    private LocalDateTime importedAt;


}
