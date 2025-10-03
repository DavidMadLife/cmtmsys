package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.ShiftSummaryDTO;
import org.chemtrovina.cmtmsys.model.ShiftSummary;
import org.chemtrovina.cmtmsys.repository.base.ShiftSummaryRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftSummaryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftSummaryServiceImpl implements ShiftSummaryService {

    private final ShiftSummaryRepository repository;

    public ShiftSummaryServiceImpl(ShiftSummaryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addSummary(ShiftSummary summary) {
        repository.add(summary);
    }

    @Override
    public void updateSummary(ShiftSummary summary) {
        repository.update(summary);
    }

    @Override
    public void deleteSummaryById(long id) {
        repository.deleteById(id);
    }

    @Override
    public ShiftSummary getSummaryById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<ShiftSummary> getAllSummaries() {
        return repository.findAll();
    }

    @Override
    public List<ShiftSummary> getSummariesByShift(int shiftId) {
        return repository.findByShift(shiftId);
    }



    @Override
    public List<ShiftSummaryDTO> getShiftSummary(LocalDate date, String shiftType, List<String> lineNames) {
        if (date != null && shiftType != null && lineNames != null && !lineNames.isEmpty()) {
            return repository.findByDateShiftAndLines(date, shiftType, lineNames);
        } else if (date != null && shiftType != null) {
            return repository.findByDateAndShiftType(date, shiftType);
        } else if (date != null) {
            return repository.findByDate(date);
        } else if (shiftType != null) {
            return repository.findByShiftType(shiftType);
        } else {
            return repository.findAllDTO();
        }
    }


}
