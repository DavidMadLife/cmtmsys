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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PcbPerformanceLogServiceImpl implements PcbPerformanceLogService {

    private final PcbPerformanceLogRepository repository;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ProductionGapLogRepository productionGapLogRepository;
    private final ShiftSummaryRepository shiftSummaryRepository;
    private final ProductCycleTimeRepository productCycleTimeRepository;
    private final ShiftScheduleSMTRepository shiftScheduleSMTRepository;

    // 👇 thêm các service cần để trừ liệu
    private final ProductBOMService productBOMService;
    private final ModelLineService modelLineService;
    private final ModelLineRunService modelLineRunService;
    private final FeederService feederService;
    private final FeederAssignmentService feederAssignmentService;
    private final FeederAssignmentMaterialService feederAssignmentMaterialService;
    private final MaterialService materialService;
    private final MaterialConsumeDetailLogService consumeDetailLogService;

    public PcbPerformanceLogServiceImpl(PcbPerformanceLogRepository repository,
                                        ProductService productService,
                                        WarehouseService warehouseService,
                                        ProductionGapLogRepository productionGapLogRepository,
                                        ShiftSummaryRepository shiftSummaryRepository,
                                        ProductCycleTimeRepository productCycleTimeRepository,
                                        ShiftScheduleSMTRepository shiftScheduleSMTRepository,
                                        // 👇 inject thêm
                                        ProductBOMService productBOMService,
                                        ModelLineService modelLineService,
                                        ModelLineRunService modelLineRunService,
                                        FeederService feederService,
                                        FeederAssignmentService feederAssignmentService,
                                        FeederAssignmentMaterialService feederAssignmentMaterialService,
                                        MaterialService materialService,
                                        MaterialConsumeDetailLogService consumeDetailLogService) {
        this.repository = repository;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.productionGapLogRepository = productionGapLogRepository;
        this.shiftSummaryRepository = shiftSummaryRepository;
        this.productCycleTimeRepository = productCycleTimeRepository;
        this.shiftScheduleSMTRepository = shiftScheduleSMTRepository;

        // 👇 gán thêm
        this.productBOMService = productBOMService;
        this.modelLineService = modelLineService;
        this.modelLineRunService = modelLineRunService;
        this.feederService = feederService;
        this.feederAssignmentService = feederAssignmentService;
        this.feederAssignmentMaterialService = feederAssignmentMaterialService;
        this.materialService = materialService;
        this.consumeDetailLogService = consumeDetailLogService;
    }


   /* @Override
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
    }*/
   @Override
   public void saveLog(PcbPerformanceLog log) {
       // 1) Lưu log mới
       repository.add(log);

       // 1.1) TRỪ LIỆU REALTIME DỰA THEO BOM + FEEDER ĐANG ACTIVE
       try {
           consumeRealtime(log);
       } catch (Exception ex) {
           // Không chặn luồng; log lỗi để còn điều tra
           // (tuỳ bạn có Logger thì dùng logger.error)
           System.err.println("[consumeRealtime] " + ex.getMessage());
       }

       // 2. Luôn cập nhật POR trước tiên (Production Output)
       int porQty = log.getTotalModules();
       int porTimeSec = 0; // nếu chưa có prev thì chưa có khoảng thời gian
       if (porQty > 0) {
           var currentShift = shiftScheduleSMTRepository.findCurrentShift(
                   log.getWarehouseId(),
                   log.getCreatedAt()
           );
           if (currentShift != null) {
               var summaries = shiftSummaryRepository.findByShift(currentShift.getShiftId());
               ShiftSummary summary = summaries.isEmpty() ? new ShiftSummary() : summaries.get(0);

               if (summaries.isEmpty()) {
                   summary.setShiftId(currentShift.getShiftId());
                   summary.setWarehouseId(log.getWarehouseId());
                   summary.setCreatedAt(LocalDateTime.now());
                   shiftSummaryRepository.add(summary);
               }

               summary.setPorQty(summary.getPorQty() + porQty);
               summary.setPorTimeSec(summary.getPorTimeSec() + porTimeSec);
               shiftSummaryRepository.update(summary);
           }
       }

       // 3. Lấy log trước đó (nếu có)
       PcbPerformanceLog prev = repository.findPrevLog(
               log.getWarehouseId(),
               log.getCreatedAt()
       );
       if (prev == null) return; // chỉ bỏ qua phần gap nếu không có log trước

       // 4. Tính PID distance
       int pidDistanceSec = (int) Duration
               .between(prev.getCreatedAt(), log.getCreatedAt())
               .getSeconds();

       // 5. Tạo gap log
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
           } else if (pidDistanceSec <= ctSec * 2) {
               gap.setStatus("TOR");
           } else if (pidDistanceSec > ctSec * 2 && pidDistanceSec <= 900) {
               gap.setStatus("IDLE");
           } else {
               gap.setStatus("IDLE");
               gap.setReason("Problem");
           }
       } else {
           gap.setStatus("IDLE");
       }
       productionGapLogRepository.add(gap);

       // 6. Cập nhật các trạng thái khác
       var currentShift = shiftScheduleSMTRepository.findCurrentShift(
               log.getWarehouseId(),
               log.getCreatedAt()
       );
       if (currentShift == null) return;

       var summaries = shiftSummaryRepository.findByShift(currentShift.getShiftId());
       ShiftSummary summary = summaries.isEmpty() ? new ShiftSummary() : summaries.get(0);
       if (summaries.isEmpty()) {
           summary.setShiftId(currentShift.getShiftId());
           summary.setWarehouseId(log.getWarehouseId());
           summary.setCreatedAt(LocalDateTime.now());
           shiftSummaryRepository.add(summary);
       }

       switch (gap.getStatus()) {
           case "TOR" -> {
               summary.setTorQty(summary.getTorQty() + 1);
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

       // POR tổng hợp lại từ tất cả các loại
       int totalTime = summary.getTorTimeSec() + summary.getIdleTimeSec() + summary.getMcTimeSec();
       int totalQty = summary.getTorQty() + summary.getIdleQty() + summary.getMcQty();
       summary.setPorTimeSec(summary.getTorTimeSec() + summary.getIdleTimeSec());
       summary.setPorQty(totalQty);

       // % phân bổ
       if (totalTime > 0) {
           summary.setPorPercent(100.0 * summary.getPorTimeSec() / summary.getTotalTimeSec());
           summary.setTorPercent(100.0 * summary.getTorTimeSec() / summary.getTotalTimeSec());
           summary.setIdlePercent(100.0 * summary.getIdleTimeSec() / summary.getTotalTimeSec());
           summary.setMcPercent(100.0 * summary.getMcTimeSec() / summary.getTotalTimeSec());
       }

       shiftSummaryRepository.update(summary);
   }


    private void consumeRealtime(PcbPerformanceLog log) {
        // 0) Tính good output
        int good = Math.max(0, log.getTotalModules());
        if (good <= 0) return;

        // 1) Lấy BOM của product
        List<ProductBOM> bomList = productBOMService.getByProductId(log.getProductId());
        if (bomList == null || bomList.isEmpty()) return;

        // 2) Resolve ModelLine & Run hiện tại
        ModelLine modelLine = modelLineService.findOrCreateModelLine(log.getProductId(), log.getWarehouseId());

        // Ưu tiên run đang chạy; nếu không có thì lấy run mới nhất
        ModelLineRun run = modelLineRunService.getActiveRun(modelLine.getModelLineId());
        if (run == null) {
            List<ModelLineRun> runs = modelLineRunService.getRunsByModelLineId(modelLine.getModelLineId());
            if (runs != null && !runs.isEmpty()) {
                run = runs.get(0); // bạn đang ưu tiên run đầu tiên ở UI
            }
        }
        if (run == null) {
            // Không có run → vẫn có thể trừ theo feeder hiện trạng; hoặc bỏ qua trừ
            // Tuỳ policy, ở đây mình vẫn cố trừ theo feeder hiện có
            System.err.println("[consumeRealtime] Không tìm thấy Run cho modelLine=" + modelLine.getModelLineId());
        }

        // 3) Lấy danh sách feeder theo model + line
        List<Feeder> feeders = feederService.getFeedersByModelAndLine(log.getProductId(), log.getWarehouseId());

        // Group feeder theo SapCode để match nhanh với BOM
        Map<String, List<Feeder>> feedersBySap = feeders.stream()
                .collect(Collectors.groupingBy(f -> f.getSapCode() == null ? "" : f.getSapCode().trim().toUpperCase()));

        // 4) Duyệt BOM và trừ dần
        for (ProductBOM bom : bomList) {
            String sap = (bom.getSappn() == null) ? "" : bom.getSappn().trim().toUpperCase();
            if (sap.isEmpty()) continue;

            // Lượng cần trừ = Qty/board * Good
            int need = (int)Math.round(bom.getQuantity() * good);
            if (need <= 0) continue;

            List<Feeder> sapFeeders = feedersBySap.getOrDefault(sap, List.of());
            if (sapFeeders.isEmpty()) {
                // Không có feeder nào gắn SAP này → bỏ qua
                continue;
            }

            // Trừ lần lượt theo các feeder đang gắn SAP đó
            for (Feeder feeder : sapFeeders) {
                if (need <= 0) break;

                // Lấy assignment theo run + feeder
                Integer assignmentId = null;
                if (run != null) {
                    FeederAssignment assignment = feederAssignmentService.getAssignment(run.getRunId(), feeder.getFeederId());
                    if (assignment != null) {
                        assignmentId = assignment.getAssignmentId();
                    }
                }

                // Nếu không có run/assignment: vẫn có thể tìm cuộn active theo feederId
                List<FeederAssignmentMaterial> matLinks;
                if (assignmentId != null) {
                    matLinks = feederAssignmentMaterialService.getMaterialsByAssignment(assignmentId);
                } else {
                    // fallback: lấy active theo feederId
                    matLinks = feederAssignmentMaterialService.getActiveByFeederId(feeder.getFeederId());
                }

                // Lọc những cuộn đang active (isActive = true && detachedAt null), chọn theo thứ tự gắn gần nhất
                List<FeederAssignmentMaterial> active = matLinks.stream()
                        .filter(m -> m.isActive() && m.getDetachedAt() == null)
                        .sorted(Comparator.comparing(FeederAssignmentMaterial::getAttachedAt)) // cũ trước, mới sau
                        .collect(Collectors.toList());

                for (FeederAssignmentMaterial link : active) {
                    if (need <= 0) break;

                    Material mat = materialService.getMaterialById(link.getMaterialId());
                    if (mat == null) continue;

                    int available = Math.max(0, mat.getQuantity());
                    if (available <= 0) continue;

                    int take = Math.min(need, available);
                    if (take <= 0) continue;

                    // 4.1) Ghi consume detail (trace)
                    MaterialConsumeDetailLog detail = new MaterialConsumeDetailLog();
                    detail.setPlanItemId(null); // theo yêu cầu: cho phép NULL
                    detail.setRunDate(log.getCreatedAt().toLocalDate());
                    detail.setMaterialId(mat.getMaterialId());
                    detail.setConsumedQty(take);
                    detail.setCreatedAt(LocalDateTime.now());
                    detail.setSourceLogId(log.getLogId()); // 👈 để trace ngược theo carrier/logId

                    consumeDetailLogService.addLog(detail);

                    // 4.2) Cập nhật tồn cuộn
                    mat.setQuantity(available - take);
                    materialService.updateMaterial(mat);

                    need -= take;
                }
            }

            // Nếu còn thiếu (need > 0): hoặc log warning, hoặc để lần sau
            if (need > 0) {
                System.err.println("[consumeRealtime] Thiếu linh kiện cho SAP=" + sap + ", còn thiếu=" + need);
            }
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
    public List<PcbPerformanceLogHistoryDTO> searchLogs(
            String modelCode,
            ModelType modelType,
            LocalDateTime from,
            LocalDateTime to,
            Integer warehouseId
    )
    {
        return repository.searchLogDTOs(modelCode, modelType, from, to, warehouseId);
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

    @Override
    public PcbPerformanceLog getByCarrierId(String carrierId) {
        return repository.findByCarrierId(carrierId);
    }

    @Override
    public List<PcbPerformanceLogHistoryDTO> getLogsByCarrierId(String carrierId) {
        return repository.getLogsByCarrierId(carrierId);
    }


}
