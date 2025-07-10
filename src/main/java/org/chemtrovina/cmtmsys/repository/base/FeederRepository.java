package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Feeder;
import java.util.List;

public interface FeederRepository {
    void add(Feeder feeder);
    void update(Feeder feeder);
    void deleteById(int feederId);
    Feeder findById(int feederId);
    List<Feeder> findAll();

    Feeder findByModelLineIdAndFeederCodeAndSapCode(int modelLineId, String feederCode, String sapCode);
    List<Feeder> findByModelAndLine(int productId, int warehouseId);
    List<Feeder> search(int productId, int warehouseId, String feederCode, String sapCode);
}
