package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Product;

import java.util.List;

public interface ProductRepository {
    void add(Product product);
    void update(Product product);
    void deleteById(int productId);
    Product findById(int productId);
    List<Product> findAll();


}
