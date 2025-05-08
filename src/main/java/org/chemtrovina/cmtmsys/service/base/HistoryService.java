package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.History;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HistoryService {
    void addHistory(History history);

    void updateHistory(History history);

    List<History> getAllHistory();

    Optional<History> getHistoryById(int id);

    List<History> searchHistory(LocalDate date, String sapPN, String status);
    void createHistoryForScannedMakePN(String makerPN, String employeeId, String scanCode);
    void deleteById(int id);

    //List<HistorySummaryViewModel> getSummaryBySapPN();
    boolean isValidMakerPN(String makerPN);

    List<History> searchHistory(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL);

    boolean isScanning(String scanCode, String makerPN);

    String extractRealMakerPN(String makerPNInput);

}
