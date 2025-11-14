package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.SparePart;
import java.util.List;

public interface SparePartRepository {
    void add(SparePart sparePart);
    void update(SparePart sparePart);
    void deleteById(int id);
    SparePart findById(int id);
    List<SparePart> findAll();
    List<SparePart> findByName(String name);
    SparePart findByCode(String code);
}
