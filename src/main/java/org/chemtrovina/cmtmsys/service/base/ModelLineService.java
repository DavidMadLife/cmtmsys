package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ModelLine;
import java.util.List;

public interface ModelLineService {
    void addModelLine(ModelLine modelLine);
    void updateModelLine(ModelLine modelLine);
    void deleteModelLineById(int id);
    ModelLine getModelLineById(int id);
    List<ModelLine> getAllModelLines();
}
