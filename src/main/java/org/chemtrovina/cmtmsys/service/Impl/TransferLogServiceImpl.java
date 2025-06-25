package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.TransferLogDto;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.TransferLog;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.base.TransferLogRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransferLogServiceImpl implements TransferLogService {

    private final TransferLogRepository repo;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;

    public TransferLogServiceImpl(TransferLogRepository repo, WarehouseService warehouseService, MaterialService materialService) {
        this.repo = repo;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
    }

    @Override
    public void addTransfer(TransferLog log) {
        repo.add(log);
    }

    @Override
    public List<TransferLog> getAllTransfers() {
        return repo.findAll();
    }

    @Override
    public List<TransferLog> getTransfersByRollCode(String rollCode) {
        return repo.findByRollCode(rollCode);
    }

    @Override
    public List<TransferLogDto> getAllTransferLogDtos() {
        List<TransferLog> logs = repo.findAll();
        Map<Integer, String> warehouseMap = warehouseService.getAllWarehouses()
                .stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseId, Warehouse::getName));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return logs.stream().map(log -> {
            Material material = materialService.getMaterialByRollCode(log.getRollCode());
            String spec = material != null ? material.getSpec() : "N/A";
            String sapCode = material != null ? material.getSapCode() : "N/A";

            return new TransferLogDto(
                    log.getRollCode(),
                    warehouseMap.getOrDefault(log.getFromWarehouseId(), "Unknown"),
                    warehouseMap.getOrDefault(log.getToWarehouseId(), "Unknown"),
                    log.getTransferDate().format(formatter),
                    log.getEmployeeId(),
                    spec,
                    sapCode
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<TransferLog> searchTransfers(String sapCode, String barcode, Integer fromWarehouse, Integer toWarehouse, LocalDateTime fromDate, LocalDateTime toDate) {
        return repo.search(sapCode, barcode, fromWarehouse, toWarehouse, fromDate, toDate);
    }


}
