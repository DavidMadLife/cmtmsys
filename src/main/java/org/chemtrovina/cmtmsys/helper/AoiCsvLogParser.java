package org.chemtrovina.cmtmsys.helper;

import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.Product;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.function.Consumer;

@Component
public class AoiCsvLogParser {

    public PcbPerformanceLog parse(
            Path file,
            String carrierId,
            Product product,
            int warehouseId,
            Consumer<String> logCallback
    ) throws IOException {

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                logCallback.accept("❌ File không có header: " + file.getFileName());
                return null;
            }

            // Tách hàm riêng:
            int[] indexes = getColumnIndexes(headerLine);
            if (indexes == null) {
                logCallback.accept("❌ Header thiếu cột bắt buộc.");
                return null;
            }

            int total = 0, ng = 0;
            String aoi = null;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length <= indexes[2]) continue;

                String judgement = parts[indexes[2]].trim().toUpperCase();
                if (aoi == null) aoi = parts[indexes[0]].trim();
                if (!judgement.equals("OK") && !judgement.equals("PASS")) ng++;
                total++;
            }

            if ("H01040056C".equalsIgnoreCase(product.getProductCode()) || "H01040058A".equalsIgnoreCase(product.getProductCode())) {
                logCallback.accept("✂️ Model đặc biệt H01040056C: chia đôi total & NG");
                total /= 2;
                ng /= 2;
            }

            if (total == 0) return null;

            double performance = ((double) (total - ng) / total) * 100;

            return new PcbPerformanceLog(0, product.getProductId(), warehouseId, carrierId, aoi,
                    total, ng, performance, file.getFileName().toString(), LocalDateTime.now());
        }
    }

    private int[] getColumnIndexes(String headerLine) {
        String[] headers = headerLine.split(",");
        int idxAoi = 0;
        int idxCarrier = -1;
        int idxJudgement = -1;

        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().toLowerCase();
            if (h.contains("carrier")) idxCarrier = i;
            else if (h.contains("judgement")) idxJudgement = i;
        }

        if (idxCarrier == -1 || idxJudgement == -1) return null;
        return new int[]{idxAoi, idxCarrier, idxJudgement};
    }
}
