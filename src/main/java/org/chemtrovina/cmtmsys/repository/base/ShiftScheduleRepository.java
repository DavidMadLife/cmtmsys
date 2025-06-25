package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ShiftSchedule;

import java.time.LocalDate;
import java.util.List;

public interface ShiftScheduleRepository {
    void add(ShiftSchedule schedule);
    void update(ShiftSchedule schedule);
    void deleteById(int id);
    ShiftSchedule findById(int id);
    List<ShiftSchedule> findAll();
    List<ShiftSchedule> findByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to);
}
