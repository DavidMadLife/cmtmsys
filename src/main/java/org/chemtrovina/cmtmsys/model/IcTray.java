package org.chemtrovina.cmtmsys.model;

import jakarta.persistence.*;
import lombok.*;
import org.chemtrovina.cmtmsys.model.enums.TrayStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "IcTray")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IcTray {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sapCode;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Column(nullable = false)
    private Integer qty;

    // ROM status
    @Builder.Default
    private Boolean isProgrammed = false;

    private LocalDateTime programmedAt;

    // Location (no FK)
    @Column(nullable = false)
    private Long currentLocationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrayStatus status;

    @Builder.Default
    private Boolean isActive = true;

    // Split tracking (no FK)
    private Long parentTrayId;

    private LocalDateTime splitAt;

    private String splitReason;

    private String remark;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}