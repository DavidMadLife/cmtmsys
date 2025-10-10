package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.EBoardSet;
import java.util.List;

public interface EBoardSetRepository {
    void add(EBoardSet set);
    void update(EBoardSet set);
    void delete(int setId);
    EBoardSet findById(int id);
    EBoardSet findByName(String name);
    List<EBoardSet> findAll();
}
