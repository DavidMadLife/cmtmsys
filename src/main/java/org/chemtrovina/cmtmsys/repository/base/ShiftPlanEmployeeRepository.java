package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;

import java.time.LocalDate;
import java.util.List;

public interface ShiftPlanEmployeeRepository {

    List<ShiftPlanEmployee> findAll();

    List<ShiftPlanEmployee> findByEmployee(int employeeId);

    List<ShiftPlanEmployee> findByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to);

    int insert(ShiftPlanEmployee plan);

    void batchInsert(List<ShiftPlanEmployee> plans);

    int delete(int shiftPlanId);

    int deleteByEmployee(int employeeId);

    int deleteByDateRange(LocalDate from, LocalDate to);

    int deleteByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to);

    int saveOrUpdate(int employeeId, LocalDate date, String shiftCode, String note);

    String findShiftCodeByEmployeeAndDate(int employeeId, LocalDate date);

    List<ShiftPlanEmployee> findByShiftDate(LocalDate date);

    int updateNote(int employeeId, LocalDate date, String note);


}

