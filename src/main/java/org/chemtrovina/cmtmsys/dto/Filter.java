package org.chemtrovina.cmtmsys.dto;

import lombok.Data;
import org.chemtrovina.cmtmsys.model.enums.LeaveType;

import java.time.LocalDate;

@Data
public class Filter {
    private LocalDate fromDate;
    private LocalDate toDate;
}

