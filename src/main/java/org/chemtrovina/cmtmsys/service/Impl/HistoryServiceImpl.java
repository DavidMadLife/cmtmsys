package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.History;
import org.chemtrovina.cmtmsys.model.MOQ;
import org.chemtrovina.cmtmsys.repository.base.HistoryRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.base.HistoryService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
                        (status == null || history.getStatus().equals(status)))
                .toList();
    }

    @Override
    public void createHistoryForScannedMakePN(String makerPNInput, String employeeId, String scanCode) {
        // Tách đúng mã MakerPN từ chuỗi bị "dính"
        String realMakerPN = extractRealMakerPN(makerPNInput);

        if (realMakerPN != null) {
            MOQ moq = moqRepository.findByMakerPN(realMakerPN);

            if (moq != null) {
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();

                History history = new History();
                history.setMaker(moq.getMaker());
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

        // Làm sạch input (loại bỏ ký tự không phải chữ số và chữ cái)
        String cleanedInput = makerPNInput.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        System.out.println("Cleaned input: " + cleanedInput);
        System.out.println("Danh sách MakerPN trong DB: ");
        allMakerPNs.forEach(System.out::println);


        return allMakerPNs.stream()
                .map(String::toUpperCase) // normalize tất cả MakerPN trong DB
                .filter(cleanedInput::contains) // kiểm tra xem chuỗi input có chứa MakerPN không
                .max((a, b) -> Integer.compare(a.length(), b.length())) // lấy chuỗi dài nhất (khả năng đúng cao hơn)
                .orElse(null);


    }
}
