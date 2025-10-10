package org.chemtrovina.cmtmsys.helper;

import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

@Component
public class AoiEBoardCsvLogParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm", Locale.ENGLISH);

    public EBoardPerformanceLog parse(Path csvPath, int setId, int warehouseId, int eBoardProductId, Consumer<String> logCallback) {
        int total = 0;
        int ng = 0;
        String aoiCode = null;
        String carrierId = null;
        String recipeName = null;

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String header = reader.readLine(); // bỏ header
            if (header == null) {
                logCallback.accept("❌ File trống: " + csvPath.getFileName());
                return null;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 9) continue;

                aoiCode = parts[0].trim();
                carrierId = parts[2].trim();
                recipeName = parts[6].trim();
                String judgement = parts[7].trim().toUpperCase();

                if (judgement.equals("OK") || judgement.equals("PASS"))
                    total++;
                else if (!judgement.isEmpty()) {
                    total++;
                    ng++;
                }
            }

            if (total == 0) {
                logCallback.accept("⚠️ File không có dòng hợp lệ: " + csvPath.getFileName());
                return null;
            }

            double performance = ((double)(total - ng) / total) * 100;

            // Phân tích recipe name
            String circuitType = parseCircuitType(recipeName);
            String modelType = parseModelType(recipeName);

            logCallback.accept("📂 Recipe: " + recipeName + " | Circuit=" + circuitType + " | Model=" + modelType);
            logCallback.accept("📊 Tổng=" + total + ", NG=" + ng + ", Hiệu suất=" + performance);

            EBoardPerformanceLog log = new EBoardPerformanceLog();
            log.seteBoardProductId(eBoardProductId);
            log.setSetId(setId);
            log.setWarehouseId(warehouseId);
            log.setCircuitType(circuitType);
            log.setModelType(modelType);
            log.setCarrierId(carrierId);
            log.setAoiMachineCode(aoiCode);
            log.setTotalModules(total);
            log.setNgModules(ng);
            log.setPerformance(performance);
            log.setLogFileName(csvPath.getFileName().toString());
            log.setCreatedAt(LocalDateTime.now());
            log.setUpdatedAt(LocalDateTime.now());

            return log;

        } catch (IOException ex) {
            logCallback.accept("❌ Lỗi đọc file: " + ex.getMessage());
            return null;
        }
    }

    private String parseCircuitType(String recipeName) {
        if (recipeName == null) return "UNKNOWN";
        recipeName = recipeName.toUpperCase();
        if (recipeName.contains("_LED") || recipeName.contains(" LED")) return "LED";
        if (recipeName.contains("_PD") || recipeName.contains(" PD")) return "PD";
        return "UNKNOWN";
    }

    private String parseModelType(String recipeName) {
        if (recipeName == null) return "SINGLE";
        recipeName = recipeName.toUpperCase();
        if (recipeName.contains("BOT")) return "BOT";
        if (recipeName.contains("TOP")) return "TOP";
        return "SINGLE";
    }
}
