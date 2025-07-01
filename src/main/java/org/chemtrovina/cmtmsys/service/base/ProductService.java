package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Product;

import java.util.List;

public interface ProductService {
    void addProduct(Product product);
    void updateProduct(Product product);
    void deleteProductById(int id);
    Product getProductById(int id);
    List<Product> getAllProducts();
    Product getProductByCode(String code);


}
