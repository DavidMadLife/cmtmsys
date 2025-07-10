package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ShiftSchedule;
import org.chemtrovina.cmtmsys.repository.base.ShiftScheduleRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShiftScheduleServiceImpl implements ShiftScheduleService {

    private final ShiftScheduleRepository repo;

    public ShiftScheduleServiceImpl(ShiftScheduleRepository repo) {
        this.repo = repo;
    }

    @Override
    public void add(ShiftSchedule schedule) {
        repo.add(schedule);
    }

    @Override
    public void update(ShiftSchedule schedule) {
        repo.update(schedule);
    }

    @Override
    public void delete(int id) {
        repo.deleteById(id);
    }

    @Override
    public ShiftSchedule getById(int id) {
        return repo.findById(id);
    }

    @Override
    public List<ShiftSchedule> getAll() {
        return repo.findAll();
    }

    @Override
    public List<ShiftSchedule> getByEmployeeInRange(int employeeId, LocalDate from, LocalDate to) {
        return repo.findByEmployeeAndDateRange(employeeId, from, to);
    }
}
