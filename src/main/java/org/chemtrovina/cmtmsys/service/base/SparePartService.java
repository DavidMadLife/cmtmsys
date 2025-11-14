package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.SparePart;
import java.util.List;

public interface SparePartService {
    void addSparePart(SparePart sparePart);
    void updateSparePart(SparePart sparePart);
    void deleteSparePartById(int id);
    SparePart getSparePartById(int id);
    List<SparePart> getAllSpareParts();
    List<SparePart> findByName(String name);
    SparePart findByCode(String code);
}
