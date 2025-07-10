package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeLogRepository;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanDailyRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class ProductionPlanDailyServiceImpl implements ProductionPlanDailyService {

    private final ProductionPlanDailyRepository repository;
    private final MaterialConsumeLogRepository logRepo;
    private final MaterialRepository materialRepo;

    public ProductionPlanDailyServiceImpl(ProductionPlanDailyRepository repository, MaterialConsumeLogRepository logRepo, MaterialRepository materialRepo) {
        this.repository = repository;
        this.logRepo = logRepo;
        this.materialRepo = materialRepo;
    }

    @Override
    public void addDaily(ProductionPlanDaily daily) {
        repository.add(daily);
    }

    @Override
    public void updateDaily(ProductionPlanDaily daily) {
        repository.update(daily);
    }

    @Override
    public void deleteDaily(int dailyId) {
        repository.deleteById(dailyId);
    }

    @Override
    public List<ProductionPlanDaily> getDailyByItemId(int planItemId) {
        return repository.findByPlanItemId(planItemId);
    }

    @Override
    public List<ProductionPlanDaily> getDailyByItemIdAndDateRange(int planItemId, LocalDate start, LocalDate end) {
        return repository.findByPlanItemIdAndDateRange(planItemId, start, end);
    }

    @Override
    public List<DailyPlanRowDto> getDailyPlanView(String lineName, int weekNo, int year) {
        return repository.getDailyPlansByLineAndWeek(lineName, weekNo, year);
    }

    @Override
    public void updateDailyPlanAndActual(int planItemId, String runDate, int planQty, int actualQty) {
        repository.updateDailyPlanAndActual(planItemId, runDate, planQty, actualQty);
    }

    @Override
    public int getTotalDailyPlanByPlanItemId(int planItemId) {
        return repository.getTotalDailyPlanByPlanItemId(planItemId);
    }

    @Override
    public int getPlannedWeeklyQuantityByPlanItemId(int planItemId) {
        return repository.getPlannedWeeklyQuantityByPlanItemId(planItemId);
    }

    @Override
    public void updateDailyPlan(int planItemId, LocalDate runDate, int planQty) {
        repository.updateDailyPlan(planItemId, runDate, planQty);
    }

    @Override
    public void updateActual(int planItemId, LocalDate runDate, int actualQty) {
        repository.updateActual(planItemId, runDate, actualQty);
    }

    @Override
    public List<DailyPlanRowDto> getDailyPlanViewForAllLines(int week, int year) {
        return repository.getDailyPlanViewForAllLines(week, year);
    }

    @Override
    public void consumeMaterialByActual(int planItemId, LocalDate runDate, int actualQty) {
        if (logRepo.exists(planItemId, runDate)) return;
        repository.consumeMaterialByActual(planItemId, runDate, actualQty);
        logRepo.insert(planItemId, runDate, actualQty);
    }

    @Override
    public int getActualQuantity(int planItemId, LocalDate runDate) {
        return repository.getActualQuantity(planItemId, runDate);
    }

    @Override
    public void rollbackConsumeMaterial(int planItemId, LocalDate runDate) {
        if (!logRepo.exists(planItemId, runDate)) return;

        int consumedQty = logRepo.getConsumedQty(planItemId, runDate);
        materialRepo.restore(planItemId, consumedQty);
        logRepo.delete(planItemId, runDate);
    }



}
