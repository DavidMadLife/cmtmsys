package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanHourlyRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanHourlyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionPlanHourlyServiceImpl implements ProductionPlanHourlyService {

    private final ProductionPlanHourlyRepository hourlyRepo;

    @Autowired
    public ProductionPlanHourlyServiceImpl(ProductionPlanHourlyRepository hourlyRepo) {
        this.hourlyRepo = hourlyRepo;
    }

    @Override
    public void saveHourlyPlan(ProductionPlanHourly plan) {
        hourlyRepo.insert(plan);
    }

    @Override
    public void updatePlanQty(int id, int newPlanQty) {
        hourlyRepo.updatePlanQuantity(id, newPlanQty);
    }

    @Override
    public void updateActualQty(int id, int newActualQty) {
        hourlyRepo.updateActualQuantity(id, newActualQty);
    }

    @Override
    public List<ProductionPlanHourly> getByDailyId(int dailyId) {
        return hourlyRepo.findByDailyId(dailyId);
    }

    @Override
    public void deleteByDailyId(int dailyId) {
        hourlyRepo.deleteByDailyId(dailyId);
    }

    @Override
    public List<ProductionPlanHourly> getHourlyPlansByDailyId(int dailyId) {
        return hourlyRepo.findByDailyId(dailyId);
    }

    @Override
    public List<ProductionPlanHourly> getHourlyPlansByDateAndLine(LocalDate date, String line) {
        return hourlyRepo.findByDateAndLine(date, line);
    }

    @Override
    public void saveOrUpdate(ProductionPlanHourly plan) {
        hourlyRepo.saveOrUpdate(plan);
    }

    @Override
    public void saveOrUpdateBulk(List<ProductionPlanHourly> plans) {
        hourlyRepo.saveOrUpdateBulk(plans);
    }
}
