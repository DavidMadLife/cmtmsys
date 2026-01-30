package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.ProductRepository;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addProduct(Product product) {
        if (repository.checkProductExists(product.getProductCode(), product.getModelType())) {
            throw new IllegalArgumentException("❌ Sản phẩm đã tồn tại (trùng ProductCode + ModelType).");
        }
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

        List<Product> products = repository.findAll();
        System.out.println("Products count: " + products.size());
        for (Product p : products) {
            System.out.println(p.getProductId() + " - " + p.getProductCode() + " - " + p.getModelType());
        }
        return products;
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

    @Override
    public String getProductCodeByPlanItemId(int planItemId) {
        return repository.findProductCodeByPlanItemId(planItemId);
    }

    @Override
    public Product getProductByNameAndType(String productName, ModelType modelType) {
        return repository.findByNameAndModelType(productName, modelType);
    }

    @Override
    public Product getProductByCodeOrNameAndType(String code, String name, ModelType modelType) {
        Product product = null;

        if (code != null && !code.isBlank()) {
            product = repository.findByCodeAndModelType(code, modelType);
        }

        if (product == null && name != null && !name.isBlank()) {
            product = repository.findByNameAndModelType(name, modelType);
        }

        return product;
    }

    @Override
    public Map<String, String> findProductNamesByCodeAndModelType(Set<String> keys) {
        return repository.findProductNamesByCodeAndModelType(keys);
    }


}
