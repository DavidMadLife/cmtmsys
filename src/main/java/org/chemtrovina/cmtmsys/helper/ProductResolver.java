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
        // 1) ưu tiên resolve theo carrier nếu có
        if (carrierId != null && !carrierId.isBlank()) {
            Product p = resolveFromCarrier(carrierId, logFileName, logCallback);
            if (p != null) return p;
        }

        // 2) fallback theo recipe -> modelName
        if (recipeName == null || recipeName.isBlank()) {
            logCallback.accept("❌ RecipeName is blank, cannot resolve Product.");
            return null;
        }

        String modelName = extractModelFromRecipe(recipeName);
        if (modelName == null || modelName.isBlank()) {
            logCallback.accept("❌ Cannot extract modelName from RecipeName: " + recipeName);
            return null;
        }

        ModelType fileModelType = detectModelTypeFromText(recipeName + " " + logFileName);
        logCallback.accept("🧩 Extract modelName from recipe: " + modelName);
        logCallback.accept("📂 Detect modelType: " + fileModelType);

        // Bạn cần 1 API lấy hết product hoặc tìm theo keyword.
        // Nếu chưa có, tạm dùng getAllProducts().
        List<Product> all = productService.getAllProducts();
        if (all == null || all.isEmpty()) {
            logCallback.accept("❌ Product table is empty.");
            return null;
        }

        // match theo name: "contain" và ưu tiên match dài nhất
        String modelNorm = normalize(modelName);

        Product best = all.stream()
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .filter(p -> normalize(p.getName()).contains(modelNorm) || modelNorm.contains(normalize(p.getName())))
                .max(Comparator.comparingInt(p -> commonLength(normalize(p.getName()), modelNorm)))
                .orElse(null);

        if (best == null) {
            logCallback.accept("❌ Cannot find Product by modelName (contain): " + modelName);
            return null;
        }

        // optional: nếu bạn muốn ưu tiên modelType khớp
        // (chỉ áp dụng nếu DB của bạn có set modelType đúng)
        Product typeMatched = all.stream()
                .filter(p -> p.getName() != null && !p.getName().isBlank())
                .filter(p -> normalize(p.getName()).contains(modelNorm) || modelNorm.contains(normalize(p.getName())))
                .filter(p -> p.getModelType() == fileModelType)
                .max(Comparator.comparingInt(p -> commonLength(normalize(p.getName()), modelNorm)))
                .orElse(best);

        logCallback.accept("✅ Matched Product by recipe: " + typeMatched.getProductCode()
                + " | name=" + typeMatched.getName()
                + " | modelType=" + typeMatched.getModelType());

        return typeMatched;
    }

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
        return s.toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .trim();
    }

    private int commonLength(String a, String b) {
        // score đơn giản: độ dài chuỗi match (min len nếu contain)
        if (a.contains(b)) return b.length();
        if (b.contains(a)) return a.length();
        return 0;
    }
}
