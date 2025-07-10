package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductBOMServiceImpl implements ProductBOMService {

    private final ProductBOMRepository repository;

    public ProductBOMServiceImpl(ProductBOMRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addProductBOM(ProductBOM bom) {
        repository.add(bom);
    }

    @Override
    public void updateProductBOM(ProductBOM bom) {
        repository.update(bom);
    }

    @Override
    public void deleteProductBOMByProductId(int productId) {
        repository.deleteByProductId(productId);
    }

    @Override
    public List<ProductBOM> getByProductId(int productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public List<ProductBomDto> getByProductCode(String productCode) {
        return repository.findBomDtoByProductCode(productCode);
    }

    @Override
    public List<ProductBomDto> getBomDtoByProductCode(String productCode) {
        return repository.findBomDtoByProductCode(productCode);
    }



}
