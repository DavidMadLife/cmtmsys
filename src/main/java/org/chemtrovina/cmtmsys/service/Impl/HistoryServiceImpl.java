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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class HistoryServiceImpl implements HistoryService {
    private final HistoryRepository historyRepository;
    private final MOQRepository moqRepository;

    public HistoryServiceImpl(HistoryRepository historyRepository, MOQRepository moqRepository) {
        this.historyRepository = historyRepository;
        this.moqRepository = moqRepository;
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
    public void createHistoryForScannedMakePN(String makerPNInput, String employeeId, String scanCode, int InvoiceId) {
        String realMakerPN = extractRealMakerPN(makerPNInput);

        if (realMakerPN != null) {
            MOQ moq = moqRepository.findByMakerPN(realMakerPN);

            if (moq != null) {
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();

                History history = new History();
                history.setMaker(moq.getMaker());
                history.setInvoiceId(InvoiceId);
                history.setMakerPN(realMakerPN);
                history.setSapPN(moq.getSapPN());
                history.setQuantity(moq.getMoq());
                history.setDate(currentDate);
                history.setTime(currentTime);
                history.setEmployeeId(employeeId);
                history.setScanCode(scanCode);
                history.setMSL(moq.getMsql());
                history.setStatus("Scanned");

                addHistory(history);
            } else {
                System.out.println("Không tìm thấy MOQ cho MakerPN: " + realMakerPN);
            }
        } else {
            System.out.println("Không thể nhận diện MakerPN từ chuỗi: " + makerPNInput);
        }


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
                        0,
                        0,
                        ""
                ))
                .collect(Collectors.toList());
    }

    public int getTotalScannedQuantityBySapPN(String sapPN, int invoiceId) {
        return historyRepository.getTotalScannedQuantityBySapPN(sapPN, invoiceId);
    }

    @Override
    // Trong HistoryService
    public List<HistoryDetailViewDto> getHistoryByInvoiceId(int invoiceId) {
        // Giả sử bạn sử dụng JPA để truy vấn dữ liệu
        List<History> historyList = historyRepository.findByInvoiceId(invoiceId);

        // Chuyển đổi từ List<History> thành List<HistoryDetailViewDto>
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
    public void deleteById(int id) {
        historyRepository.delete(id);
    }



    @Override
    public boolean isValidMakerPN(String makerPN) {
        return moqRepository.findByMakerPN(makerPN) != null;
    }

    @Override
    public List<History> searchHistory(String invoiceNo, String maker, String makerPN, String sapPN, LocalDate date, String MSL) {
        return historyRepository.search(invoiceNo, maker, makerPN, sapPN, date, MSL);
    }

    @Override
    public boolean isScanning(String scanCode, String makerPN) {
        return historyRepository.existsByScanCodeAndMakerPN(scanCode, makerPN);
    }

    @Override
    public String extractRealMakerPN(String makerPNInput) {
        List<String> allMakerPNs = moqRepository.findAllMakerPNs();
        // Normalize input: bỏ ký tự không phải chữ-số, viết hoa
        // Normalize input
        String cleanedInput = makerPNInput.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        System.out.println("Cleaned input: " + cleanedInput);

        return allMakerPNs.stream()
                .filter(dbMPN -> {
                    String normalized = dbMPN.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                    return cleanedInput.contains(normalized);
                })
                .max(Comparator.comparingInt(mpn -> mpn.replaceAll("[^A-Za-z0-9]", "").length()))
                .orElse(null); // Trả về MakerPN gốc (có khoảng trắng) để dùng với DB

    }
}
