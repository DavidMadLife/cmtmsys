package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.PcbPerformanceLogHistoryDTO;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.repository.base.PcbPerformanceLogRepository;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class PcbPerformanceLogServiceImpl implements PcbPerformanceLogService {

    private final PcbPerformanceLogRepository repository;
    private final ProductService productService;
    private final WarehouseService warehouseService;


    public PcbPerformanceLogServiceImpl(PcbPerformanceLogRepository repository, ProductService productService, WarehouseService warehouseService) {
        this.repository = repository;
        this.productService = productService;
        this.warehouseService = warehouseService;
    }

    @Override
    public void saveLog(PcbPerformanceLog log) {
        repository.add(log);
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
