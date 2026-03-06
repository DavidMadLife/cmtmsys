package org.chemtrovina.cmtmsys.dto;

import lombok.Data;
import org.chemtrovina.cmtmsys.model.enums.ScanResult;

import java.time.LocalDateTime;

@Data
public class MaterialMakerScanDto {
    private String rollCode;
    private String spec;
    private String maker;
    private String makerPNInput;
    private String expectedMakerPN;
    private ScanResult result;
    private String message;
    private LocalDateTime scanAt;
}
