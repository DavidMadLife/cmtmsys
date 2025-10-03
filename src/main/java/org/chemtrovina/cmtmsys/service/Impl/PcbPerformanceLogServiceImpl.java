package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.*;
import org.chemtrovina.cmtmsys.service.base.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PcbPerformanceLogServiceImpl implements PcbPerformanceLogService {

    private final PcbPerformanceLogRepository repository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ProductionGapLogRepository productionGapLogRepository;
    private final ShiftSummaryRepository shiftSummaryRepository;
    private final ProductCycleTimeRepository productCycleTimeRepository;
    private final ShiftScheduleSMTRepository shiftScheduleSMTRepository;

    public PcbPerformanceLogServiceImpl(PcbPerformanceLogRepository repository,
                                        ProductService productService,
                                        WarehouseService warehouseService,
                                        ProductionGapLogRepository productionGapLogRepository,
                                        ShiftSummaryRepository shiftSummaryRepository,
                                        ProductCycleTimeRepository productCycleTimeRepository,
                                        ShiftScheduleSMTRepository shiftScheduleSMTRepository) {
        this.repository = repository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.productionGapLogRepository = productionGapLogRepository;
        this.shiftSummaryRepository = shiftSummaryRepository;
        this.productCycleTimeRepository = productCycleTimeRepository;
        this.shiftScheduleSMTRepository = shiftScheduleSMTRepository;
    }

    @Override

    public void saveLog(PcbPerformanceLog log) {
        // 1. Lưu log mới
        repository.add(log);

        // 2. Lấy log trước đó
        PcbPerformanceLog prev = repository.findPrevLog(
                log.getWarehouseId(),
                log.getProductId(),
                log.getCreatedAt()
        );
        if (prev == null) return;

        // 3. Tính khoảng cách PID
        int pidDistanceSec = (int) Duration
                .between(prev.getCreatedAt(), log.getCreatedAt())
                .getSeconds();

        // 4. Tạo gap log
        var gap = new ProductionGapLog();
        gap.setProductId(log.getProductId());
        gap.setWarehouseId(log.getWarehouseId());
        gap.setPrevLogId(prev.getLogId());
        gap.setCurrLogId(log.getLogId());
        gap.setPidDistanceSec(pidDistanceSec);
        gap.setCreatedAt(LocalDateTime.now());

        var ct = productCycleTimeRepository.findActive(log.getProductId(), log.getWarehouseId());

        if (ct != null) {
            int ctSec = ct.getCtSeconds().intValue();

            if (prev.getProductId() != log.getProductId()) {
                gap.setStatus("M/C");
                gap.setReason("Model Change - Different Product");
            }
            else if (pidDistanceSec <= ctSec * 2) {
                gap.setStatus("TOR");
            }
            else if (pidDistanceSec > ctSec * 2 && pidDistanceSec <= 900) {
                gap.setStatus("IDLE");
            }
            else {
                gap.setStatus("M/C");
                gap.setReason("Model Change - Timeout");
            }
        } else {
            gap.setStatus("IDLE");
        }

        productionGapLogRepository.add(gap);

        // 5. Cập nhật Summary
        var currentShift = shiftScheduleSMTRepository.findCurrentShift(
                log.getWarehouseId(),
                log.getCreatedAt()
        );

        if (currentShift != null) {
            var summaries = shiftSummaryRepository.findByShift(currentShift.getShiftId());
            ShiftSummary summary;

            if (summaries.isEmpty()) {
                summary = new ShiftSummary();
                summary.setShiftId(currentShift.getShiftId());
                summary.setWarehouseId(log.getWarehouseId());
                summary.setCreatedAt(LocalDateTime.now());
                shiftSummaryRepository.add(summary);
            } else {
                summary = summaries.get(0);
            }

            // POR luôn cộng (final output)
            int porQty = log.getTotalModules() - log.getNgModules();
            if (porQty > 0) {
                summary.setPorQty(summary.getPorQty() + porQty);
                summary.setPorTimeSec(summary.getPorTimeSec() + pidDistanceSec);
            }

            switch (gap.getStatus()) {
                case "TOR" -> {
                    summary.setTorQty(summary.getTorQty() + log.getTotalModules());
                    summary.setTorTimeSec(summary.getTorTimeSec() + pidDistanceSec);
                }
                case "IDLE" -> {
                    summary.setIdleQty(summary.getIdleQty() + 1);
                    summary.setIdleTimeSec(summary.getIdleTimeSec() + pidDistanceSec);
                }
                case "M/C" -> {
                    summary.setMcQty(summary.getMcQty() + 1);
                    summary.setMcTimeSec(summary.getMcTimeSec() + pidDistanceSec);
                }
            }

            // Tính lại %
            int total = summary.getPorTimeSec() + summary.getTorTimeSec() +
                    summary.getIdleTimeSec() + summary.getMcTimeSec();
            if (total > 0) {
                summary.setPorPercent(100.0 * summary.getPorTimeSec() / total);
                summary.setTorPercent(100.0 * summary.getTorTimeSec() / total);
                summary.setIdlePercent(100.0 * summary.getIdleTimeSec() / total);
                summary.setMcPercent(100.0 * summary.getMcTimeSec() / total);
            }

            shiftSummaryRepository.update(summary);
        }
    }




    @Override
    public boolean isAlreadyProcessed(String carrierId) {
        return repository.existsByCarrierId(carrierId);
    }

    @Override
    public List<PcbPerformanceLog> getAllLogs() {
        return repository.findAll();
    }

    @Override
    public List<PcbPerformanceLog> getLogsByProductId(int productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public List<PcbPerformanceLog> getLogsByWarehouseId(int warehouseId) {
        return repository.findByWarehouseId(warehouseId);
    }

    @Override
    public List<PcbPerformanceLogHistoryDTO> searchLogs(String modelCode, ModelType modelType,
                                                        LocalDateTime from, LocalDateTime to) {
        return repository.searchLogDTOs(modelCode, modelType, from, to);
    }


    @Override
    public boolean isFileAlreadySaved(String fileName) {
        return repository.existsByFileName(fileName);
    }


    @Override
    public List<PcbPerformanceLogHistoryDTO> fetchPerformanceGoodModules(String lineName, LocalDateTime start, LocalDateTime end) {
        // Lấy warehouseId từ tên line
        Integer warehouseId = warehouseService.getIdByName(lineName);
        if (warehouseId == null) return Collections.emptyList();

        return repository.getLogsByWarehouseAndDateRange(warehouseId, start, end);
    }

}
