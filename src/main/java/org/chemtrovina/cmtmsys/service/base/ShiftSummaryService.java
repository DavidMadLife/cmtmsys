package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.model.ShiftSummary;

import java.time.LocalDate;
import java.util.List;

public interface ShiftSummaryService {
    void addSummary(ShiftSummary summary);
    void updateSummary(ShiftSummary summary);
    void deleteSummaryById(long id);
    ShiftSummary getSummaryById(long id);
    List<ShiftSummary> getAllSummaries();
    List<ShiftSummary> getSummariesByShift(int shiftId);

    List<ShiftSummaryDTO> getShiftSummary(LocalDate date, String shiftType, List<String> lineNames);

}
