package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;

import java.nio.file.Path;
import java.util.List;

public interface EBoardPerformanceLogRepository {
    void add(EBoardPerformanceLog log);
    List<EBoardPerformanceLog> findAll();
    List<EBoardPerformanceLog> findBySet(int setId);
    List<EBoardPerformanceLog> findBySetAndCircuit(int setId, String circuitType);
    EBoardPerformanceLog findLatestBySetAndCircuit(int setId, String circuitType);


}
