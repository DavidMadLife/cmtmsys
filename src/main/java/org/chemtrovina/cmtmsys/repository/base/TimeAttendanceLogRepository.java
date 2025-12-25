package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.TimeAttendanceLog;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;

import java.time.LocalDate;
import java.util.List;

public interface TimeAttendanceLogRepository {
    List<TimeAttendanceLog> findAll();
    TimeAttendanceLog findById(int id);
    int insert(TimeAttendanceLog log);
    int update(TimeAttendanceLog log);
    int delete(int id);

    List<TimeAttendanceLog> findByScanDateRange(LocalDate from, LocalDate to);

    List<TimeAttendanceLog> findByEmployeeIdAndDate(int employeeId, LocalDate date);

    TimeAttendanceLog findByEmployeeIdDateAndAction(
            int employeeId,
            LocalDate date,
            ScanAction action
    );


}
