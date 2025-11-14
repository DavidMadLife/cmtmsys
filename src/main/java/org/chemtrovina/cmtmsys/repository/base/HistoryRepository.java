package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.History;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HistoryRepository extends GenericRepository<History>{
    List<History> search(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL, String InvoicePN);
    boolean existsByScanCodeAndMakerPN(String scanCode, String makerPN);
    List<History> findByInvoiceId(int invoiceId);
    int getTotalScannedQuantityBySapPN(String sapPN, int invoiceId);
    void deleteLastByMakerPNAndInvoiceId(String makerPN, int invoiceId);
    void deleteLastBySapPNAndInvoiceId(String sapPN, int invoiceId);
}
