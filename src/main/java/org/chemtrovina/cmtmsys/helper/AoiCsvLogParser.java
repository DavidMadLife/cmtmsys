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

        System.out.println("=================================================");
        System.out.println("📥 START PARSE FILE: " + file.getFileName());
        System.out.println("CarrierId = " + carrierId);
        System.out.println("Product = " + (product != null ? product.getProductCode() : "NULL"));

        try (BufferedReader reader = Files.newBufferedReader(file)) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("❌ HEADER NULL");
                logCallback.accept("❌ File không có header: " + file.getFileName());
                return null;
            }

            System.out.println("🧾 HEADER = " + headerLine);

            int[] idx = getColumnIndexes(headerLine);
            if (idx == null) {
                System.out.println("❌ HEADER INVALID (missing Carrier/Judgement)");
                logCallback.accept("❌ Header thiếu cột bắt buộc (Carrier/Judgement).");
                return null;
            }

            int idxAoi = idx[0];
            int idxCarrier = idx[1];
            int idxJudgement = idx[2];
            int idxRecipe = idx[3];
            int idxModuleId = idx[4];

            System.out.println("📌 INDEXES:");
            System.out.println("   AOI=" + idxAoi
                    + ", Carrier=" + idxCarrier
                    + ", Judgement=" + idxJudgement
                    + ", Recipe=" + idxRecipe
                    + ", ModuleId=" + idxModuleId);

            int totalRows = 0;
            int ngRows = 0;

            String aoi = null;
            String recipeUpper = null;
            int maxModuleId = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                String delimiter = headerLine.contains("\t") ? "\t" : ",";
                String[] parts = line.split(java.util.regex.Pattern.quote(delimiter), -1);
                if (parts.length <= idxJudgement) continue;

                if (aoi == null && parts.length > idxAoi) {
                    aoi = parts[idxAoi].trim();
                }

                if (recipeUpper == null && idxRecipe != -1 && parts.length > idxRecipe) {
                    String r = parts[idxRecipe].trim();
                    if (!r.isEmpty()) recipeUpper = r.toUpperCase();
                }

                String judgement = parts[idxJudgement].trim().toUpperCase();
                if (!judgement.equals("OK") && !judgement.equals("PASS")) {
                    ngRows++;
                }

                if (idxModuleId != -1 && parts.length > idxModuleId) {
                    String m = parts[idxModuleId].trim();
                    if (!m.isEmpty()) {
                        try {
                            int mid = Integer.parseInt(m);
                            maxModuleId = Math.max(maxModuleId, mid);
                        } catch (Exception ignored) {}
                    }
                }

                totalRows++;
            }

            System.out.println("📊 RAW COUNT:");
            System.out.println("   totalRows = " + totalRows);
            System.out.println("   ngRows    = " + ngRows);
            System.out.println("   recipe    = " + recipeUpper);
            System.out.println("   maxModuleId = " + maxModuleId);

            if (totalRows == 0) {
                System.out.println("⚠️ totalRows = 0 -> SKIP FILE");
                return null;
            }

            int divisor = detectDivisor(recipeUpper);
            System.out.println("🧮 divisor (by recipe) = " + divisor);

            if (divisor == 1 && recipeUpper == null && maxModuleId == totalRows && totalRows % 2 == 0) {
                System.out.println("🟡 HEURISTIC HIT: moduleId == totalRows & even -> divide by 2");
                logCallback.accept("🟡 Recipe missing, heuristic: ModuleId suggests dual-side => divide by 2");
                divisor = 2;
            }

            int total = (int) Math.ceil((double) totalRows / divisor);
            int ng = (int) Math.ceil((double) ngRows / divisor);

            double performance = ((double) (total - ng) / total) * 100;

            System.out.println("✅ FINAL RESULT:");
            System.out.println("   PCB Total = " + total);
            System.out.println("   PCB NG    = " + ng);
            System.out.println("   Yield     = " + performance + "%");

            System.out.println("=================================================");

            logCallback.accept(String.format(
                    "📊 File=%s | Recipe=%s | Rows=%d | NGRows=%d | Divisor=%d => PCB=%d | NG=%d | Yield=%.2f%%",
                    file.getFileName(),
                    (recipeUpper == null ? "(null)" : recipeUpper),
                    totalRows, ngRows, divisor, total, ng, performance
            ));

            return new PcbPerformanceLog(
                    0,
                    product.getProductId(),
                    warehouseId,
                    carrierId,
                    aoi,
                    total,
                    ng,
                    performance,
                    file.getFileName().toString(),
                    LocalDateTime.now(),
                    0
            );
        }
    }

    /**
     * return indexes: [idxAoi, idxCarrier, idxJudgement, idxRecipe, idxModuleId]
     */
    private int[] getColumnIndexes(String headerLine) {
        String[] headers = headerLine.split(",");

        int idxAoi = 0;
        int idxCarrier = -1;
        int idxJudgement = -1;
        int idxRecipe = -1;
        int idxModuleId = -1;

        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().toLowerCase();

            if (h.contains("carrier")) idxCarrier = i;
            else if (h.contains("judgement")) idxJudgement = i;
            else if (h.contains("recipe")) idxRecipe = i;
            else if (h.contains("module") && h.contains("id")) idxModuleId = i;
        }

        return (idxCarrier == -1 || idxJudgement == -1)
                ? null
                : new int[]{idxAoi, idxCarrier, idxJudgement, idxRecipe, idxModuleId};
    }

    private int detectDivisor(String recipeUpper) {
        if (recipeUpper == null) return 1;

        if (recipeUpper.contains("TOP-BOT")
                || recipeUpper.contains("BOT-TOP")
                || recipeUpper.contains("BOTTOP")
                || recipeUpper.contains("TOPBOT")) {
            return 2;
        }

        if (recipeUpper.contains("-TOP") || recipeUpper.contains(" TOP")) return 1;
        if (recipeUpper.contains("-BOT") || recipeUpper.contains(" BOT")) return 1;

        return 1;
    }


}
