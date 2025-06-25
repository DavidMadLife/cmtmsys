package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.service.base.ProductService;

import java.util.List;

public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addProduct(Product product) {
        repository.add(product);
    }

    @Override
    public void updateProduct(Product product) {
        repository.update(product);
    }

    @Override
    public void deleteProductById(int id) {
        repository.deleteById(id);
    }

    @Override
    public Product getProductById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }


}
