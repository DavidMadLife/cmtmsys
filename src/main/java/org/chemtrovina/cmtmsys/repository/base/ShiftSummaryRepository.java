package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.model.ShiftSummary;

import java.time.LocalDate;
import java.util.List;

public interface ShiftSummaryRepository {
    void add(ShiftSummary summary);
    void update(ShiftSummary summary);
    void deleteById(long summaryId);
    ShiftSummary findById(long summaryId);
    List<ShiftSummary> findAll();
    List<ShiftSummary> findByShift(int shiftId);

    List<ShiftSummaryDTO> findByDateAndShiftType(LocalDate date, String shiftType);
    List<ShiftSummaryDTO> findByDate(LocalDate date);
    List<ShiftSummaryDTO> findByShiftType(String shiftType);
    List<ShiftSummaryDTO> findAllDTO();
    List<ShiftSummaryDTO> findByDateShiftAndLines(LocalDate date, String shiftType, List<String> lineNames);

}
