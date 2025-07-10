package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;

import java.util.List;

public interface ProductBOMService {
    void addProductBOM(ProductBOM bom);
    void updateProductBOM(ProductBOM bom);
    void deleteProductBOMByProductId(int productId);
    List<ProductBOM> getByProductId(int productId);
    List<ProductBomDto> getByProductCode(String productCode);
    List<ProductBomDto> getBomDtoByProductCode(String productCode);


}
