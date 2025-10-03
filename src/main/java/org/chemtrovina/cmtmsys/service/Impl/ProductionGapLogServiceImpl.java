package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ProductionGapLog;
import org.chemtrovina.cmtmsys.repository.base.ProductionGapLogRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionGapLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionGapLogServiceImpl implements ProductionGapLogService {

    private final ProductionGapLogRepository repository;

    public ProductionGapLogServiceImpl(ProductionGapLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addGapLog(ProductionGapLog gapLog) {
        repository.add(gapLog);
    }

    @Override
    public void deleteGapLogById(long id) {
        repository.deleteById(id);
    }

    @Override
    public ProductionGapLog getGapLogById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<ProductionGapLog> getAllGapLogs() {
        return repository.findAll();
    }

    @Override
    public List<ProductionGapLog> getGapLogsByShift(int warehouseId, String start, String end) {
        return repository.findByShift(warehouseId, start, end);
    }
}
