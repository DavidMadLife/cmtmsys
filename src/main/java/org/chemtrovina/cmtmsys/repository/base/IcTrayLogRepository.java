package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.IcTrayLog;
import org.chemtrovina.cmtmsys.model.enums.TrayAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IcTrayLogRepository extends JpaRepository<IcTrayLog, Long> {

    List<IcTrayLog> findByTrayIdOrderByCreatedAtDesc(Long trayId);

    Optional<IcTrayLog> findFirstByTrayIdOrderByCreatedAtDesc(Long trayId);

    List<IcTrayLog> findByActionAndCreatedAtBetween(TrayAction action, LocalDateTime from, LocalDateTime to);

    List<IcTrayLog> findByToLocationIdAndCreatedAtBetween(Long toLocationId, LocalDateTime from, LocalDateTime to);

    List<IcTrayLog> findByFromLocationIdAndCreatedAtBetween(Long fromLocationId, LocalDateTime from, LocalDateTime to);
}