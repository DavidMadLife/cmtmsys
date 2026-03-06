package org.chemtrovina.cmtmsys.model;

import jakarta.persistence.*;
import lombok.*;
import org.chemtrovina.cmtmsys.model.enums.LocationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "StorageLocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String code; // SMT_WH, ROM_TABLE, CHAMBER_A, LINE_01

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}