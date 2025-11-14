package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.SparePartOutput;
import java.util.List;

public interface SparePartOutputService {
    void addOutput(SparePartOutput output);
    List<SparePartOutput> getAllOutputsWithSparePart();

}
