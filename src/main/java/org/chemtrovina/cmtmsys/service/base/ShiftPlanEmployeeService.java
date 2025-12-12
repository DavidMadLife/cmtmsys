package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public interface ShiftPlanEmployeeService {

    List<ShiftPlanEmployee> getAll();

    List<ShiftPlanEmployee> getByEmployee(int employeeId);

    List<ShiftPlanEmployee> getByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to);

    List<ShiftPlanEmployee> getByDateRange(LocalDate from, LocalDate to);

    void save(ShiftPlanEmployee plan);

    void saveAll(List<ShiftPlanEmployee> plans);

    void delete(int shiftPlanId);

    void deleteByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to);

    /**
     * Import phân ca từ file Excel.
     * importedBy = user hiện tại, lưu vào cột ImportedBy.
     */
    void importFromExcel(File excelFile, String importedBy);

    int saveOrUpdate(int employeeId, LocalDate date, String shiftCode, String note);
}
