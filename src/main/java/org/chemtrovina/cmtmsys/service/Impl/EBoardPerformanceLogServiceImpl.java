package org.chemtrovina.cmtmsys.service.Impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;
import org.chemtrovina.cmtmsys.repository.base.EBoardPerformanceLogRepository;
import org.chemtrovina.cmtmsys.service.base.EBoardPerformanceLogService;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EBoardPerformanceLogServiceImpl implements EBoardPerformanceLogService {

    private final EBoardPerformanceLogRepository repository;

    public EBoardPerformanceLogServiceImpl(EBoardPerformanceLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveLog(EBoardPerformanceLog log) {
        repository.add(log);
    }

    @Override
    public List<EBoardPerformanceLog> getAllLogs() {
        return repository.findAll();
    }

    @Override
    public List<EBoardPerformanceLog> getLogsBySet(int setId) {
        return repository.findBySet(setId);
    }

    @Override
    public List<EBoardPerformanceLog> getLogsBySetAndCircuit(int setId, String circuitType) {
        return repository.findBySetAndCircuit(setId, circuitType);
    }

    @Override
    public EBoardPerformanceLog getLatestLogBySetAndCircuit(int setId, String circuitType) {
        return repository.findLatestBySetAndCircuit(setId, circuitType);
    }

    @Override
    public Map<String, Double> calculatePerformanceSummary(int setId) {
        List<EBoardPerformanceLog> logs = repository.findBySet(setId);
        if (logs.isEmpty()) return Collections.emptyMap();

        Map<String, List<EBoardPerformanceLog>> grouped = new HashMap<>();

        for (EBoardPerformanceLog log : logs) {
            String key = log.getCircuitType() + "_" + log.getModelType(); // Ví dụ: LED_TOP
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(log);
        }

        Map<String, Double> summary = new LinkedHashMap<>();
        for (var entry : grouped.entrySet()) {
            List<EBoardPerformanceLog> group = entry.getValue();
            int total = group.stream().mapToInt(EBoardPerformanceLog::getTotalModules).sum();
            int ng = group.stream().mapToInt(EBoardPerformanceLog::getNgModules).sum();
            double perf = (total == 0) ? 0 : ((double)(total - ng) / total) * 100;
            summary.put(entry.getKey(), Math.round(perf * 100.0) / 100.0);
        }

        return summary;
    }

    @Override
    public Path exportToExcel(int setId, Path savePath) {
        List<EBoardPerformanceLog> logs = repository.findBySet(setId);
        if (logs.isEmpty()) return null;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("EBoard Logs");

            // Style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header
            Row header = sheet.createRow(0);
            String[] headers = {
                    "SetId", "EBoardProductId", "CircuitType", "ModelType", "WarehouseId",
                    "TotalModules", "NgModules", "Performance", "LogFileName", "CreatedAt"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Dữ liệu
            int rowIdx = 1;
            for (EBoardPerformanceLog log : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getSetId());
                row.createCell(1).setCellValue(log.geteBoardProductId());
                row.createCell(2).setCellValue(log.getCircuitType());
                row.createCell(3).setCellValue(log.getModelType());
                row.createCell(4).setCellValue(log.getWarehouseId());
                row.createCell(5).setCellValue(log.getTotalModules());
                row.createCell(6).setCellValue(log.getNgModules());
                row.createCell(7).setCellValue(log.getPerformance());
                row.createCell(8).setCellValue(log.getLogFileName());
                row.createCell(9).setCellValue(
                        log.getCreatedAt() != null ? fmt.format(log.getCreatedAt()) : ""
                );
            }

            // Auto-size column
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(savePath.toFile())) {
                workbook.write(fos);
            }

            return savePath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
