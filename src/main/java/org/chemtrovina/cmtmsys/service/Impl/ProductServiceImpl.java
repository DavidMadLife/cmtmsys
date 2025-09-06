package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        repository.updateProduct(product);
    }

    @Override
    public void deleteProductWithBOM(int productId) {
        repository.deleteProductWithBOM(productId);
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
    @Override
    public Product getProductByCode(String code) {
        return repository.getProductByCode(code); // Giả sử bạn đã có hàm này trong repository
    }

    @Override
    public boolean checkProductExists(String productCode, ModelType modelType) {
        return repository.checkProductExists(productCode, modelType);
    }


    @Override
    public Product getProductByCodeAndType(String productCode, ModelType modelType) {
        return repository.findByCodeAndModelType(productCode, modelType);
    }

    @Override
    public List<Product> getProductsByCodeContainedInText(String text) {
        return repository.findAllByCodeContainedInText(text);
    }


}
