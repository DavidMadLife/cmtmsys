package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.SparePartOutput;
import java.util.List;

public interface SparePartOutputRepository {
    void add(SparePartOutput output);
    List<SparePartOutput> findAll();
    List<SparePartOutput> findByDateRange(String from, String to);
}
