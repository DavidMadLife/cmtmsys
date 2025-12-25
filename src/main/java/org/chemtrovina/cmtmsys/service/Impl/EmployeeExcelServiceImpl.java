package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.*;
import org.chemtrovina.cmtmsys.dto.EmployeeExcelDto;
import org.chemtrovina.cmtmsys.service.base.EmployeeExcelService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeExcelServiceImpl implements EmployeeExcelService {

    @Override
    public List<EmployeeExcelDto> readEmployeeExcel(File file) {

        List<EmployeeExcelDto> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                EmployeeExcelDto dto = new EmployeeExcelDto();

                dto.setMscnId1(getCellString(row.getCell(1), evaluator));
                dto.setFullName(getCellString(row.getCell(2), evaluator));
                dto.setCompany(getCellString(row.getCell(3), evaluator));
                dto.setDepartmentName(getCellString(row.getCell(4), evaluator));
                dto.setGender(getCellString(row.getCell(5), evaluator));
                dto.setPositionName(getCellString(row.getCell(6), evaluator));
                dto.setJobTitle(getCellString(row.getCell(7), evaluator));
                dto.setManager(getCellString(row.getCell(8), evaluator));
                dto.setBirthDate(getCellDate(row.getCell(9), evaluator));
                dto.setEntryDate(getCellDate(row.getCell(10), evaluator));
                dto.setPhoneNumber(getCellString(row.getCell(11), evaluator));
                dto.setNote("");

                // Bỏ dòng rỗng
                if (dto.getMscnId1() == null || dto.getMscnId1().isBlank()) {
                    continue;
                }

                result.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel nhân viên", e);
        }

        return result;
    }

    // ================== Helper ==================

    private String getCellString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        CellType type = cell.getCellType() == CellType.FORMULA
                ? evaluator.evaluateFormulaCell(cell)
                : cell.getCellType();

        switch (type) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return null;
        }
    }

    private LocalDate getCellDate(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        try {
            CellType type = cell.getCellType() == CellType.FORMULA
                    ? evaluator.evaluateFormulaCell(cell)
                    : cell.getCellType();

            // Excel DATE
            if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            // TEXT DATE: dd/MM/yyyy
            if (type == CellType.STRING) {
                String text = cell.getStringCellValue().trim();
                if (text.isEmpty()) return null;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(text, formatter);
            }

        } catch (Exception e) {
            throw new RuntimeException("Sai định dạng ngày tại ô: " + cell.getAddress(), e);
        }

        return null;
    }
}
