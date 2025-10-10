package org.chemtrovina.cmtmsys.helper;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public Product resolveFromCarrierAndFileName(String carrierId, String logFileName, Consumer<String> logCallback) {
        if (carrierId == null || carrierId.isBlank()) return null;

        List<Product> candidates = productService.getProductsByCodeContainedInText(carrierId);
        if (candidates == null || candidates.isEmpty()) {
            logCallback.accept("❌ Không tìm thấy sản phẩm nào khớp với CarrierID: " + carrierId);
            return null;
        }

        // ✅ Xác định modelType từ tên file log
        ModelType fileModelType = detectModelTypeFromFileName(logFileName);
        logCallback.accept("📂 Phát hiện modelType từ file: " + fileModelType);

        // Ưu tiên sản phẩm có modelType khớp
        Product matched = candidates.stream()
                .filter(p -> p.getModelType() == fileModelType)
                .findFirst()
                .orElse(candidates.get(0));

        logCallback.accept("🔗 Match ProductCode: " + matched.getProductCode()
                + " (modelType: " + matched.getModelType() + ")");
        return matched;
    }

    private ModelType detectModelTypeFromFileName(String fileName) {
        if (fileName == null) return ModelType.SINGLE;

        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.contains("bottop") || lower.contains("topbot") ||
                (lower.contains("top") && lower.contains("bot"))) {
            return ModelType.BOTTOP;
        } else if (lower.contains("top")) {
            return ModelType.TOP;
        } else if (lower.contains("bot")) {
            return ModelType.BOT;
        } else {
            return ModelType.SINGLE;
        }
    }
}
