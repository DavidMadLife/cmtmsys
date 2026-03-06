package org.chemtrovina.cmtmsys.model;

import jakarta.persistence.*;
import lombok.*;
import org.chemtrovina.cmtmsys.model.enums.TrayAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "IcTrayLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IcTrayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrayAction action;

    // no FK
    @Column(nullable = false)
    private Long trayId;

    private String barcodeSnapshot;
    private String sapCodeSnapshot;

    private Long fromLocationId;
    private Long toLocationId;

    private Integer qtyBefore;
    private Integer qtyAfter;
    private Integer qtyChange;

    private Boolean isProgrammedBefore;
    private Boolean isProgrammedAfter;

    private String refType; // INVOICE, LOT, PLAN
    private String refNo;

    private String message;

    private String createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();
}