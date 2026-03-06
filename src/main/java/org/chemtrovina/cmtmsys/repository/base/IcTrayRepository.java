package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.IcTray;
import org.chemtrovina.cmtmsys.model.enums.TrayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IcTrayRepository extends JpaRepository<IcTray, Long> {

    Optional<IcTray> findByBarcode(String barcode);

    Optional<IcTray> findByBarcodeAndIsActive(String barcode, Boolean isActive);

    boolean existsByBarcodeAndIsActive(String barcode, Boolean isActive);

    List<IcTray> findByCurrentLocationIdAndIsActive(Long currentLocationId, Boolean isActive);

    List<IcTray> findBySapCodeAndIsActive(String sapCode, Boolean isActive);

    List<IcTray> findByStatusAndIsActive(TrayStatus status, Boolean isActive);

    List<IcTray> findByIsProgrammedAndIsActive(Boolean isProgrammed, Boolean isActive);

    List<IcTray> findByParentTrayId(Long parentTrayId); // lấy tray con khi split

    Optional<IcTray> findByTrayCode(String trayCode);
}