package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ModelLine;
import java.util.List;

public interface ModelLineRepository {
    void add(ModelLine modelLine);
    void update(ModelLine modelLine);
    void deleteById(int modelLineId);
    ModelLine findById(int modelLineId);
    List<ModelLine> findAll();

    ModelLine findOrCreateModelLine(int productId, int warehouseId);


}
