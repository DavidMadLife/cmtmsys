package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.util.List;


public interface ProductRepository {
    void add(Product product);
    void update(Product product);
    void deleteById(int productId);
    Product findById(int productId);
    List<Product> findAll();
    Product getProductByCode(String code);
    boolean checkProductExists(String productCode, ModelType modelType);

    Product findByCodeAndModelType(String productCode, ModelType modelType);

    void updateProduct(Product product);
    void deleteProductWithBOM(int productId);


    List<Product> findAllByCodeContainedInText(String text);

    String findProductCodeByPlanItemId(int planItemId);
    Product findByNameAndModelType(String productName, ModelType modelType);

}
