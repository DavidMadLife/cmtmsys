package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.*;
import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.model.TimeAttendanceLog;
import org.chemtrovina.cmtmsys.model.enums.ScanAction;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public interface TimeAttendanceLogService {

    List<TimeAttendanceLog> getAll();
    TimeAttendanceLog getById(int id);
    void create(TimeAttendanceLog log);
    void update(TimeAttendanceLog log);
    void delete(int id);
    List<TimeAttendanceLogDto> getLogDtosByDateRange(LocalDate from, LocalDate to);


    TimeAttendanceLogDto processScan(String input, String type);
    TimeAttendanceLogDto processScanAuto(String input);


    List<AbsentEmployeeDto> getAbsentEmployees(LocalDate date);

    void manualFixAttendance(
            int employeeId,
            LocalDate date,
            String time,
            ScanAction action
    );

    void applyAttendanceStatus(
            TimeAttendanceLogDto dto,
            ShiftTypeEmployee shift
    );

    void recalculateStatus(TimeAttendanceLogDto dto);

    List<EmployeeScanViewDto> getTodayScannedForEmployeeView();

    AttendanceSummaryDto getAttendanceSummary(LocalDate date);

    AttendanceImportResult importAttendanceFromExcel(File file, LocalDate workDate);



}