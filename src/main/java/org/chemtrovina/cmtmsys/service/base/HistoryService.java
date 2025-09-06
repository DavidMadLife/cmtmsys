package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.MOQ;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HistoryService {
    void addHistory(History history);

    void updateHistory(History history);

    List<History> getAllHistory();

    Optional<History> getHistoryById(int id);

    List<History> searchHistory(LocalDate date, String sapPN, String status);
    void createHistoryForScannedMakePN(MOQ moq, String employeeId, String scanCode, int invoiceId);
    void createHistoryForScanOddReel(MOQ moq, String employeeId, String scanCode, int invoiceId, int quantity);


    void deleteById(int id);

    boolean isValidMakerPN(String makerPN);

    List<History> searchHistory(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL, String invoicePN);

    boolean isScanning(String scanCode, String makerPN);

    Optional<MOQ> findMatchedMOQInInvoice(String makerPN, int invoiceId, InvoiceDetailService invoiceDetailService);

    String extractRealMakerPN(String makerPNInput);

    List<HistoryDetailViewDto> getHistoryDetailsByInvoiceId(int id);
    int getTotalScannedQuantityBySapPN(String sapPN, int invoiceId);
    List<HistoryDetailViewDto> getHistoryByInvoiceId(int invoiceId);
    void deleteLastByMakerPNAndInvoiceId(String makerPN, int invoiceId);

    int getLastScannedQuantityBySapPN(String sapPN, int invoiceId);




}
