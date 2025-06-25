package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;

import java.util.List;

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
    public List<ProductBOM> getByProductCode(String productCode) {
        return repository.findByProductCode(productCode);
    }

    @Override
    public List<ProductBomDto> getBomDtoByProductCode(String productCode) {
        List<ProductBOM> rawList = repository.findByProductCode(productCode);
        return rawList.stream().map(b -> new ProductBomDto(
                productCode,
                b.getSappn(),
                b.getQuantity(),
                b.getCreatedDate(),
                b.getUpdatedDate()
        )).toList();
    }


}
