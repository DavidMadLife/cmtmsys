package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.EBoardPerformanceLog;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface EBoardPerformanceLogService {
    void saveLog(EBoardPerformanceLog log);
    List<EBoardPerformanceLog> getAllLogs();
    List<EBoardPerformanceLog> getLogsBySet(int setId);
    List<EBoardPerformanceLog> getLogsBySetAndCircuit(int setId, String circuitType);
    EBoardPerformanceLog getLatestLogBySetAndCircuit(int setId, String circuitType);

    Map<String, Double> calculatePerformanceSummary(int setId);
    Path exportToExcel(int setId, Path savePath);
}
