package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import org.chemtrovina.cmtmsys.model.ShiftSummary;
import org.chemtrovina.cmtmsys.repository.base.ShiftScheduleSMTRepository;
import org.chemtrovina.cmtmsys.repository.base.ShiftSummaryRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftScheduleSMTService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShiftScheduleSMTServiceImpl implements ShiftScheduleSMTService {

    private final ShiftScheduleSMTRepository repository;
    private final ShiftSummaryRepository summaryRepository;

    public ShiftScheduleSMTServiceImpl(ShiftScheduleSMTRepository repository, ShiftSummaryRepository summaryRepository) {
        this.repository = repository;
        this.summaryRepository = summaryRepository;
    }

    @Override
    public void addShift(ShiftScheduleSMT shift) {
        repository.add(shift);
        ShiftSummary summary = new ShiftSummary();
        summary.setShiftId(shift.getShiftId());
        summary.setWarehouseId(shift.getWarehouseId());
        summary.setTotalTimeSec((int) java.time.Duration.between(
                shift.getStartTime(), shift.getEndTime()).getSeconds());
        summary.setTorTimeSec(0);
        summary.setPorTimeSec(0);
        summary.setIdleTimeSec(0);
        summary.setTorQty(0);
        summary.setPorQty(0);
        summary.setIdleQty(0);
        summary.setTorPercent(0);
        summary.setPorPercent(0);
        summary.setIdlePercent(0);
        summary.setCreatedAt(LocalDateTime.now());

        summaryRepository.add(summary);
    }

    @Override
    public void updateShift(ShiftScheduleSMT shift) {
        repository.update(shift);
    }

    @Override
    public void deleteShiftById(int id) {
        repository.deleteById(id);
    }

    @Override
    public ShiftScheduleSMT getShiftById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<ShiftScheduleSMT> getAllShifts() {
        return repository.findAll();
    }

    @Override
    public List<ShiftScheduleSMT> getShiftsByDate(String date) {
        return repository.findByDate(date);
    }
}
