package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ShiftSchedule;

import java.time.LocalDate;
import java.util.List;

public interface ShiftScheduleService {
    void add(ShiftSchedule schedule);
    void update(ShiftSchedule schedule);
    void delete(int id);
    ShiftSchedule getById(int id);
    List<ShiftSchedule> getAll();
    List<ShiftSchedule> getByEmployeeInRange(int employeeId, LocalDate from, LocalDate to);
}
