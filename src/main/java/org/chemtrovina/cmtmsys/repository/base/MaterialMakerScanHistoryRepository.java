package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.MaterialMakerScanHistory;
import org.chemtrovina.cmtmsys.model.enums.ScanResult;

import java.time.LocalDate;
import java.util.List;

public interface MaterialMakerScanHistoryRepository {
    List<MaterialMakerScanHistory> findAll();
    MaterialMakerScanHistory findById(int id);

    int insert(MaterialMakerScanHistory h);
    int delete(int id);

    List<MaterialMakerScanHistory> findByScanDateRange(LocalDate from, LocalDate toExclusive);
    List<MaterialMakerScanHistory> findByRollCode(String rollCode);
    List<MaterialMakerScanHistory> findByEmployeeIdAndDate(String employeeId, LocalDate date);
    List<MaterialMakerScanHistory> findByResultAndDate(ScanResult result, LocalDate date);
}
