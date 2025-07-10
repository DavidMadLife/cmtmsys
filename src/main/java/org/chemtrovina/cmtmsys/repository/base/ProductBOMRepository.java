package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;

import java.util.List;

public interface ProductBOMRepository {
    void add(ProductBOM bom);
    void update(ProductBOM bom);
    void deleteByProductId(int productId);
    List<ProductBOM> findByProductId(int productId);
    List<ProductBomDto> findBomDtoByProductCode(String productCode);
}
