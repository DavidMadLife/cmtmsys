package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.EBoardProduct;
import java.util.List;

public interface EBoardProductRepository {
    void add(EBoardProduct product);
    void update(EBoardProduct product);
    void delete(int id);
    EBoardProduct findById(int id);
    List<EBoardProduct> findBySet(int setId);
    List<EBoardProduct> findBySetAndCircuit(int setId, String circuitType);
    List<EBoardProduct> findAll();
}
