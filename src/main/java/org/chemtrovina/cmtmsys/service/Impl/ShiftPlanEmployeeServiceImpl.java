package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;
import org.chemtrovina.cmtmsys.repository.base.ShiftPlanEmployeeRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftPlanEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShiftPlanEmployeeServiceImpl implements ShiftPlanEmployeeService {

    private final ShiftPlanEmployeeRepository repo;

    public ShiftPlanEmployeeServiceImpl(ShiftPlanEmployeeRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ShiftPlanEmployee> getAll() {
        return repo.findAll();
    }

    @Override
    public List<ShiftPlanEmployee> getByEmployee(int employeeId) {
        return repo.findByEmployee(employeeId);
    }

    @Override
    public List<ShiftPlanEmployee> getByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to) {
        if (from == null || to == null) return List.of();
        return repo.findByEmployeeAndDateRange(employeeId, from, to);
    }

    @Override
    public List<ShiftPlanEmployee> getByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) return List.of();

        // nếu cần tất cả nhân viên trong khoảng ngày → dùng repo.findAll() + filter
        // hoặc viết thêm hàm repo riêng, tùy bạn.
        var all = repo.findAll();
        List<ShiftPlanEmployee> result = new ArrayList<>();
        for (var p : all) {
            if (!p.getShiftDate().isBefore(from) && !p.getShiftDate().isAfter(to)) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public void save(ShiftPlanEmployee plan) {
        if (plan == null) return;
        repo.insert(plan);
    }

    @Override
    public void saveAll(List<ShiftPlanEmployee> plans) {
        if (plans == null || plans.isEmpty()) return;
        repo.batchInsert(plans);
    }

    @Override
    public void delete(int shiftPlanId) {
        repo.delete(shiftPlanId);
    }

    @Override
    public void deleteByEmployeeAndDateRange(int employeeId, LocalDate from, LocalDate to) {
        if (from == null || to == null) return;
        repo.deleteByEmployeeAndDateRange(employeeId, from, to);
    }

    @Override
    public void importFromExcel(File excelFile, String importedBy) {
        if (excelFile == null || !excelFile.exists()) {
            throw new IllegalArgumentException("Excel file not found");
        }

        // TODO: dùng Apache POI để đọc file Excel:
        // - Map MSCNID1 / MSCNID2 → EmployeeId (cần EmployeeRepository)
        // - Đọc từng dòng, từng ngày → build List<ShiftPlanEmployee>
        // - Gọi saveAll(plans);

        // Ví dụ khung skeleton cho sau này:
        /*
        List<ShiftPlanEmployee> plans = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                // bỏ header
                if (row.getRowNum() == 0) continue;

                // đọc mã nhân viên, ngày, ca,...
                // ShiftPlanEmployee p = new ShiftPlanEmployee();
                // p.setEmployeeId(...);
                // p.setShiftDate(...);
                // p.setShiftCode(...);
                // p.setImportedBy(importedBy);
                // plans.add(p);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error reading Excel: " + ex.getMessage(), ex);
        }

        saveAll(plans);
        */
    }

    @Override
    public int saveOrUpdate(int employeeId, LocalDate date, String shiftCode, String note) {
        return repo.saveOrUpdate(employeeId, date, shiftCode, note);
    }

    @Override
    public List<ShiftPlanEmployee> findByShiftDate(LocalDate date) {
        return repo.findByShiftDate(date);
    }

    @Override
    @Transactional
    public void updateNote(int employeeId, LocalDate date, String note) {
        repo.updateNote(employeeId, date, note);
    }
}
