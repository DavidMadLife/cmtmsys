package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.util.List;

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



}
