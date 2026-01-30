package org.chemtrovina.cmtmsys.helper;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Component
public class ProductResolver {

    private final ProductService productService;

    @Autowired
    public ProductResolver(ProductService productService) {
        this.productService = productService;
    }

    public Product resolve(
            String carrierId,
            String recipeName,
            String logFileName,
            Consumer<String> logCallback
    ) {
        Product byCarrier = null;

        // ================= 1) TRY CARRIER =================
        if (carrierId != null && !carrierId.isBlank()) {
            byCarrier = resolveFromCarrier(carrierId, logFileName, logCallback);

            /*if (byCarrier != null && recipeName != null && !recipeName.isBlank()) {
                // check carrier match recipe bằng fuzzy score
                int carrierScore = score(buildFeatures(recipeName), buildFeatures(byCarrier.getName()))
                        + bonusFromFileName(logFileName, byCarrier.getName());

                if (carrierScore < 8) {
                    logCallback.accept("⚠️ Carrier resolved but NOT match recipe → fallback to Recipe (score=" + carrierScore + ")");
                    byCarrier = null;
                }
            }*/
        }

        if (byCarrier != null) {
            logCallback.accept("✅ Use Product by Carrier: " + byCarrier.getProductCode());
            return byCarrier;
        }

        // ================= 2) FALLBACK RECIPE =================
        if (recipeName == null || recipeName.isBlank()) {
            logCallback.accept("❌ RecipeName is blank, cannot resolve Product.");
            return null;
        }

        ModelType modelType = detectModelTypeFromText(recipeName + " " + logFileName);
        logCallback.accept("📂 Detect modelType: " + modelType);

        List<Product> all = productService.getAllProducts();
        if (all == null || all.isEmpty()) {
            logCallback.accept("❌ Product table empty.");
            return null;
        }

        // ✅ Features CHÍNH chỉ lấy từ recipeName để tránh nhiễu từ file/path
        var recipeFeats = buildFeatures(recipeName);

        // ✅ Gate theo modelKey letters+digits (generic)
        var recipeKeys = extractModelKeys(recipeName);

        Product best = null;
        int bestScore = Integer.MIN_VALUE;
        int secondBestScore = Integer.MIN_VALUE;

        // (optional) debug top candidates
        java.util.ArrayList<Candidate> cands = new java.util.ArrayList<>();

        for (Product p : all) {
            if (p == null || p.getName() == null || p.getName().isBlank()) continue;

            // Filter modelType nếu detect ra TOP/BOT/BOTTOP
            if (modelType != ModelType.SINGLE && p.getModelType() != modelType) continue;

            // ✅ Gate modelKey: nếu cả recipe và product đều có key thì phải giao nhau
            var productKeys = extractModelKeys(p.getName());
            if (!recipeKeys.isEmpty() && !productKeys.isEmpty() && !hasAnyCommon(recipeKeys, productKeys)) {
                continue;
            }

            var prodFeats = buildFeatures(p.getName());

            // Score chính = recipeName
            int sc = score(recipeFeats, prodFeats);

            // Bonus nhỏ từ fileName (nếu bạn vẫn muốn tận dụng chút context)
            sc += bonusFromFileName(logFileName, p.getName());

            // lưu debug
            cands.add(new Candidate(p, sc));

            // tie-breaker: score bằng nhau ưu tiên name dài hơn (cụ thể hơn)
            if (sc > bestScore || (sc == bestScore && best != null && p.getName().length() > best.getName().length())) {
                secondBestScore = bestScore;
                bestScore = sc;
                best = p;
            } else if (sc > secondBestScore) {
                secondBestScore = sc;
            }
        }

        // threshold chống match bậy
        int minScore = 8;
        int minGap = 2;

        if (best == null || bestScore < minScore || (bestScore - secondBestScore) < minGap) {
            logCallback.accept("❌ Cannot resolve Product by Recipe. bestScore=" + bestScore + ", secondBest=" + secondBestScore);

            // in top 5 candidates để bắt tận tay môi trường prod
            cands.stream()
                    .sorted((a, b) -> Integer.compare(b.score, a.score))
                    .limit(5)
                    .forEach(c -> logCallback.accept("🔍 CANDIDATE sc=" + c.score
                            + " | " + c.product.getProductCode()
                            + " | " + c.product.getName()
                            + " | " + c.product.getModelType()));
            return null;
        }

        // In top 5 candidates (để debug khi chạy thực, có thể bỏ nếu spam log)
        cands.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(5)
                .forEach(c -> logCallback.accept("🔍 CANDIDATE sc=" + c.score
                        + " | " + c.product.getProductCode()
                        + " | " + c.product.getName()
                        + " | " + c.product.getModelType()));

        logCallback.accept("✅ Matched Product by Recipe: "
                + best.getProductCode()
                + " | " + best.getName()
                + " | " + best.getModelType()
                + " | score=" + bestScore);

        return best;
    }

    /* ===================== HELPERS ===================== */

    private int bonusFromFileName(String logFileName, String productName) {
        if (logFileName == null || logFileName.isBlank() || productName == null || productName.isBlank()) return 0;

        // bonus nhỏ: fileName chỉ đóng vai trò phụ (chia 3)
        var fileFeats = buildFeatures(logFileName);
        var prodFeats = buildFeatures(productName);
        return score(fileFeats, prodFeats) / 3;
    }

    private java.util.Set<String> buildFeatures(String text) {
        List<String> toks = tokenize(text);
        java.util.Set<String> feats = new java.util.HashSet<>(toks);

        // bigram join: "touch only" -> "touchonly"
        for (int i = 0; i < toks.size() - 1; i++) {
            feats.add(toks.get(i) + toks.get(i + 1));
        }
        return feats;
    }

    private List<String> tokenize(String text) {
        if (text == null) return List.of();

        String cleaned = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        if (cleaned.isEmpty()) return List.of();

        String[] raw = cleaned.split("\\s+");

        java.util.ArrayList<String> out = new java.util.ArrayList<>(raw.length * 2);
        for (String t : raw) {
            if (t.isBlank()) continue;
            out.addAll(splitPrefixAlphaNum(t));
        }
        return out;
    }

    // tách "lq2touchonly" -> ["lq2","touchonly"] (generic)
    private List<String> splitPrefixAlphaNum(String token) {
        var m = java.util.regex.Pattern
                .compile("^([a-z]+\\d+)([a-z].*)$")
                .matcher(token);

        if (m.matches()) {
            return List.of(m.group(1), m.group(2));
        }
        return List.of(token);
    }

    private java.util.Set<String> extractModelKeys(String text) {
        var keys = new java.util.HashSet<String>();
        for (String t : tokenize(text)) {
            if (t.matches("^[a-z]+\\d+$")) {
                keys.add(t); // lq2, du7000, ku3...
            }
        }
        return keys;
    }

    private boolean hasAnyCommon(java.util.Set<String> a, java.util.Set<String> b) {
        for (String x : a) {
            if (b.contains(x)) return true;
        }
        return false;
    }

    private int score(java.util.Set<String> recipeFeats, java.util.Set<String> productFeats) {
        int s = 0;

        for (String pf : productFeats) {
            if (!recipeFeats.contains(pf)) continue;

            s += Math.min(10, pf.length());

            // bonus phân biệt mạnh (generic)
            if (pf.equals("lh") || pf.equals("rh")) s += 8;
            if (pf.equals("top") || pf.equals("bot") || pf.equals("bottop") || pf.equals("topbot")) s += 6;
        }
        return s;
    }

    private static class Candidate {
        final Product product;
        final int score;
        Candidate(Product product, int score) {
            this.product = product;
            this.score = score;
        }
    }


  /*  *//* ===================== HELPERS ===================== *//*

   *//* private java.util.Set<String> buildFeatures(String text) {
        List<String> toks = tokenize(text);
        java.util.Set<String> feats = new java.util.HashSet<>(toks);

        // bigram join: "touch only" -> "touchonly"
        for (int i = 0; i < toks.size() - 1; i++) {
            feats.add(toks.get(i) + toks.get(i + 1));
        }

        return feats;
    }

    private List<String> tokenize(String text) {
        if (text == null) return List.of();

        // replace mọi ký tự không phải chữ/số thành space (ăn luôn ';', '-', '_', '(' ')' ...)
        String cleaned = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();

        if (cleaned.isEmpty()) return List.of();

        String[] raw = cleaned.split("\\s+");

        // tách token kiểu "lq2touchonly" -> "lq2", "touchonly" (generic)
        java.util.ArrayList<String> out = new java.util.ArrayList<>(raw.length * 2);
        for (String t : raw) {
            if (t.isBlank()) continue;
            out.addAll(splitPrefixAlphaNum(t));
        }*//*

        // lọc token quá ngắn nếu muốn (tuỳ hệ thống của bạn)
        // ở đây giữ lại cả "m" vì có thể có ý nghĩa ở model khác, nhưng bạn có thể bỏ nếu nhiễu:
        // out = out.stream().filter(x -> x.length() >= 2).toList();

        return out;
    }*/

    /*// tách prefix dạng letters+digits ở đầu token, phần còn lại là chữ (generic, không hardcode)
    private List<String> splitPrefixAlphaNum(String token) {
        var m = java.util.regex.Pattern
                .compile("^([a-z]+\\d+)([a-z].*)$")
                .matcher(token);

        if (m.matches()) {
            String a = m.group(1); // lq2
            String b = m.group(2); // touchonly
            return List.of(a, b);
        }
        return List.of(token);
    }

    private int score(java.util.Set<String> recipeFeats, java.util.Set<String> productFeats) {
        int s = 0;

        for (String pf : productFeats) {
            if (!recipeFeats.contains(pf)) continue;

            // token càng dài càng đáng tin
            s += Math.min(10, pf.length());

            // bonus cho token phân biệt mạnh (generic)
            if (pf.equals("lh") || pf.equals("rh")) s += 8;
            if (pf.equals("top") || pf.equals("bot") || pf.equals("bottop") || pf.equals("topbot")) s += 6;
        }

        return s;
    }*/



    private Product resolveFromCarrier(String carrierId, String logFileName, Consumer<String> logCallback) {
        List<Product> candidates = productService.getProductsByCodeContainedInText(carrierId);
        if (candidates == null || candidates.isEmpty()) {
            logCallback.accept("❌ Không tìm thấy sản phẩm nào khớp với CarrierID: " + carrierId);
            return null;
        }

        ModelType fileModelType = detectModelTypeFromText(logFileName);
        logCallback.accept("📂 Phát hiện modelType từ file: " + fileModelType);

        Product matched = candidates.stream()
                .filter(p -> p.getModelType() == fileModelType)
                .findFirst()
                .orElse(candidates.get(0));

        logCallback.accept("🔗 Match ProductCode: " + matched.getProductCode()
                + " (modelType: " + matched.getModelType() + ")");
        return matched;
    }

    private String extractModelFromRecipe(String recipe) {
        if (recipe == null || recipe.isBlank()) return null;

        // bỏ TOP / BOT / ký tự không liên quan
        String cleaned = recipe.toUpperCase(Locale.ROOT)
                .replace("TOP-BOT", "")
                .replace("BOT-TOP", "")
                .replace("TOPBOT", "")
                .replace("BOTTOP", "")
                .replace("-TOP", "")
                .replace("-BOT", "")
                .replace("TOP", "")
                .replace("BOT", "")
                .replace("(", " ")
                .replace(")", " ")
                .replace("_", " ")
                .replace("-", " ")
                .trim();

        // ví dụ:
        // DU7000(HDWB-2470)-TOP-BOT
        // -> "DU7000 HDWB 2470"
        return cleaned;
    }


    private ModelType detectModelTypeFromText(String text) {
        if (text == null) return ModelType.SINGLE;
        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.contains("top-bot") || lower.contains("bot-top")
                || lower.contains("bottop") || lower.contains("topbot")
                || (lower.contains("top") && lower.contains("bot"))) {
            return ModelType.BOTTOP;
        } else if (lower.contains("top")) {
            return ModelType.TOP;
        } else if (lower.contains("bot")) {
            return ModelType.BOT;
        }
        return ModelType.SINGLE;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "")   // remove space, _, -, ;, (), ...
                .trim();
    }



    private int commonLength(String a, String b) {
        // score đơn giản: độ dài chuỗi match (min len nếu contain)
        if (a.contains(b)) return b.length();
        if (b.contains(a)) return a.length();
        return 0;
    }
}
