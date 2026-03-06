package org.chemtrovina.cmtmsys.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chemtrovina.cmtmsys.model.enums.ScanResult;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MaterialMakerScanHistory {
    private int id;
    private String rollCode;
    private String makerPN;          // user scanned
    private String expectedMakerPN;  // from mapping snapshot
    private String spec;             // snapshot
    private String maker;            // snapshot
    private ScanResult result;       // PASS/FAIL/NOT_FOUND/DUPLICATE
    private String message;
    private String employeeId;
    private LocalDateTime scanAt;
}
