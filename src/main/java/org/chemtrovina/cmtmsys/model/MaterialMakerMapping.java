package org.chemtrovina.cmtmsys.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMakerMapping {

    private int id;
    private String rollCode;
    private String spec;
    private String makerPN;
    private String maker;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
