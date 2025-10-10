package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.EBoardProduct;
import java.util.List;

public interface EBoardProductService {
    void addProduct(EBoardProduct product);
    void updateProduct(EBoardProduct product);
    void deleteProduct(int id);
    EBoardProduct getProductById(int id);
    List<EBoardProduct> getProductsBySet(int setId);
    List<EBoardProduct> getProductsBySetAndCircuit(int setId, String circuitType);
    List<EBoardProduct> getAllProducts();
}
