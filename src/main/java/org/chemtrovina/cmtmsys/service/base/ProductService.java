package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductService {
    void addProduct(Product product);
    void updateProduct(Product product);
    void deleteProductById(int id);
    Product getProductById(int id);
    List<Product> getAllProducts();
    Product getProductByCode(String code);
    boolean checkProductExists(String productCode, ModelType modelType);
    void deleteProductWithBOM(int productId);
    Product getProductByCodeAndType(String productCode, ModelType modelType);
    List<Product> getProductsByCodeContainedInText(String text);

    String getProductCodeByPlanItemId(int planItemId);
    Product getProductByNameAndType(String productName, ModelType modelType);
    Product getProductByCodeOrNameAndType(String code, String name, ModelType modelType);

    Map<String, String> findProductNamesByCodeAndModelType(Set<String> keys);

}
