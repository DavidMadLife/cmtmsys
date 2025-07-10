package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Feeder;

import java.io.File;
import java.util.List;

public interface FeederService {
    void addFeeder(Feeder feeder);
    void updateFeeder(Feeder feeder);
    void deleteFeederById(int id);
    Feeder getFeederById(int id);
    List<Feeder> getAllFeeders();

    List<Feeder> getFeedersByModelAndLine(int productId, int warehouseId);
    List<Feeder> searchFeeders(int productId, int warehouseId, String feederCode, String sapCode);
    void importFeedersFromExcel(File file);
}
