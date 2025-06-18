package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.HistoryDetailViewDto;
import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.Invoice;
import org.chemtrovina.cmtmsys.model.InvoiceDetail;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.base.HistoryRepository;
import org.chemtrovina.cmtmsys.repository.base.InvoiceRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.base.HistoryService;
import org.chemtrovina.cmtmsys.service.base.InvoiceDetailService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryServiceImpl implements HistoryService {
    private final HistoryRepository historyRepository;
    private final MOQRepository moqRepository;
    private final InvoiceRepository invoiceRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository, MOQRepository moqRepository, InvoiceRepository invoiceRepository) {
        this.historyRepository = historyRepository;
        this.moqRepository = moqRepository;
        this.invoiceRepository = invoiceRepository;
    }


    @Override
    public void addHistory(History history) {
        historyRepository.add(history);
    }

    @Override
    public void updateHistory(History history) {
        historyRepository.update(history);
    }

    @Override
    public List<History> getAllHistory() {
        return historyRepository.findAll();
    }

    @Override
    public Optional<History> getHistoryById(int id) {
        return Optional.ofNullable(historyRepository.findById(id));
    }

    @Override
    public List<History> searchHistory(LocalDate date, String sapPN, String status) {
        return historyRepository.findAll().stream()
                .filter(history -> (date == null || history.getDate().equals(date)) &&
                        (sapPN == null || history.getSapPN().equals(sapPN)) &&
                        (status == null || history. getStatus().equals(status)))
                .toList();
    }

    @Override
    public void createHistoryForScannedMakePN(MOQ moq, String employeeId, String scanCode, int invoiceId) {
        if (moq == null) {
            System.out.println("Không có MOQ để lưu.");
            return;
        }

        Invoice invoice = invoiceRepository.findById(invoiceId);

        History history = new History();
        history.setMaker(moq.getMaker());
        history.setInvoiceId(invoiceId);
        history.setMakerPN(moq.getMakerPN());
        history.setSapPN(moq.getSapPN());
        history.setQuantity(moq.getMoq());
        history.setDate(LocalDate.now());
        history.setTime(LocalTime.now());
        history.setEmployeeId(employeeId);
        history.setScanCode(scanCode);
        history.setMSL(moq.getMsql());
        history.setSpec(moq.getSpec());
        history.setStatus("Scanned");

        if (invoice != null) {
            history.setInvoicePN(invoice.getInvoicePN()); // Gán invoicePN
        }


        addHistory(history);
    }


    @Override
    public void createHistoryForScanOddReel(MOQ moq, String employeeId, String scanCode, int invoiceId, int quantity) {
        if (moq == null) {
            System.out.println("Không có MOQ để lưu.");
            return;
        }

        Invoice invoice = invoiceRepository.findById(invoiceId);

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        History history = new History();
        history.setMaker(moq.getMaker());
        history.setInvoiceId(invoiceId);
        history.setMakerPN(moq.getMakerPN());
        history.setSapPN(moq.getSapPN());
        history.setQuantity(quantity);
        history.setDate(currentDate);
        history.setTime(currentTime);
        history.setEmployeeId(employeeId);
        history.setScanCode(scanCode);
        history.setMSL(moq.getMsql());
        history.setSpec(moq.getSpec());
        history.setStatus("Scanned");

        if (invoice != null) {
            history.setInvoicePN(invoice.getInvoicePN()); // Gán invoicePN
        }

        addHistory(history);
    }


    public List<HistoryDetailViewDto> getHistoryDetailsByInvoiceId(int invoiceId) {
        // Truy vấn lịch sử từ repository dựa trên InvoiceNo
        List<History> historyList = historyRepository.findByInvoiceId(invoiceId);

        // Chuyển đổi List<History> thành List<HistoryDetailViewDto>
        return historyList.stream()
                .map(history -> new HistoryDetailViewDto(
                        history.getId(),
                        history.getMakerPN(),
                        history.getSapPN(),
                        history.getMaker(),
                        history.getQuantity(),
                        history.getQuantity(),
                        0,
                        "",
                        history.getSpec()
                ))
                .collect(Collectors.toList());
    }

    public int getTotalScannedQuantityBySapPN(String sapPN, int invoiceId) {
        return historyRepository.getTotalScannedQuantityBySapPN(sapPN, invoiceId);
    }

    @Override

    public List<HistoryDetailViewDto> getHistoryByInvoiceId(int invoiceId) {

        List<History> historyList = historyRepository.findByInvoiceId(invoiceId);

        List<HistoryDetailViewDto> dtoList = new ArrayList<>();
        for (History history : historyList) {
            HistoryDetailViewDto dto = new HistoryDetailViewDto();
            dto.setId(history.getId());
            dto.setMakerCode(history.getMakerPN());
            dto.setSapCode(history.getSapPN());
            dto.setMaker(history.getMaker());
            dto.setMoq(history.getQuantity());
            dto.setQty(history.getQuantity());
            dto.setReelQty(history.getQuantity() / history.getQuantity()); // Cập nhật theo logic của bạn
            dto.setInvoice("");
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    public void deleteLastByMakerPNAndInvoiceId(String makerPN, int invoiceId) {
        historyRepository.deleteLastByMakerPNAndInvoiceId(makerPN, invoiceId);
    }

    @Override
    public int getLastScannedQuantityBySapPN(String sapPN, int invoiceId) {
        List<History> historyList = historyRepository.findByInvoiceId(invoiceId).stream()
                .filter(h -> sapPN.equals(h.getSapPN()))
                .sorted(Comparator.comparing(History::getDate).thenComparing(History::getTime).reversed())
                .toList();

        if (!historyList.isEmpty()) {
            return historyList.get(0).getQuantity(); // Bản ghi mới nhất
        }

        return 0;
    }



    @Override
    public void deleteById(int id) {
        historyRepository.delete(id);
    }



    @Override
    public boolean isValidMakerPN(String makerPN) {
        return moqRepository.findByMakerPN(makerPN) != null;
    }

    @Override
    public List<History> searchHistory(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL, String invoicePN) {
        return historyRepository.search(invoiceNo, maker, makerPN, sapPN, date, MSL, invoicePN);
    }

    @Override
    public boolean isScanning(String scanCode, String makerPN) {
        return historyRepository.existsByScanCodeAndMakerPN(scanCode, makerPN);
    }

    @Override
    public Optional<MOQ> findMatchedMOQInInvoice(String makerPN, int invoiceId, InvoiceDetailService invoiceDetailService) {
        List<MOQ> moqList = moqRepository.getAllMOQsByMakerPN(makerPN);

        if (moqList == null || moqList.isEmpty()) {
            return Optional.empty();
        }

        for (MOQ moq : moqList) {
            String sapPN = moq.getSapPN();
            InvoiceDetail detail = invoiceDetailService.getInvoiceDetailBySapPNAndInvoiceId(sapPN, invoiceId);
            if (detail != null) {
                return Optional.of(moq);
            }
        }

        return Optional.of(moqList.get(0));
    }


    @Override
    public String extractRealMakerPN(String makerPNInput) {
        List<String> allMakerPNs = moqRepository.findAllMakerPNs();
        String cleanedInput = makerPNInput.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        System.out.println("Cleaned input: " + cleanedInput);
        return allMakerPNs.stream()
                .filter(dbMPN -> {
                    if (dbMPN == null || dbMPN.trim().isEmpty()) return false; // bảo vệ thêm
                    String normalized = dbMPN.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                    return cleanedInput.contains(normalized);
                })
                .max(Comparator.comparingInt(mpn -> mpn.replaceAll("[^A-Za-z0-9]", "").length()))
                .orElse(null);
    }

}
