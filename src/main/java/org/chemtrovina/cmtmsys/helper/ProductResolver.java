package org.chemtrovina.cmtmsys.helper;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ProductResolver {

    private final ProductService productService;

    @Autowired
    public ProductResolver(ProductService productService) {
        this.productService = productService;
    }

    public Product resolveFromCarrier(String carrierId, Consumer<String> logCallback) {
        if (carrierId == null || carrierId.isBlank()) return null;

        List<Product> candidates = productService.getProductsByCodeContainedInText(carrierId);
        if (candidates == null || candidates.isEmpty()) return null;

        Product p = candidates.stream()
                .filter(x -> x.getModelType() == ModelType.BOTTOP)
                .findFirst()
                .orElseGet(() -> candidates.stream()
                        .filter(x -> x.getModelType() == ModelType.SINGLE)
                        .findFirst()
                        .orElse(candidates.get(0)));

        logCallback.accept("🔗 Match ProductCode: " + p.getProductCode() + " (modelType: " + p.getModelType() + ")");
        return p;
    }
}
