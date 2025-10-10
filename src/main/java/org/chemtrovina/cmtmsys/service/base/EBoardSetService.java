package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.EBoardSet;
import java.util.List;

public interface EBoardSetService {
    void addSet(EBoardSet set);
    void updateSet(EBoardSet set);
    void deleteSet(int setId);
    EBoardSet getSetById(int setId);
    EBoardSet getSetByName(String name);
    List<EBoardSet> getAllSets();
}
