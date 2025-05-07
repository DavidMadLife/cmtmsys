package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.History;

import java.time.LocalDate;
import java.util.List;

public interface HistoryRepository extends GenericRepository<History>{
    List<History> search(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL);
    boolean existsByScanCodeAndMakerPN(String scanCode, String makerPN);
}
