package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.ProductionPlan;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private final ProductionPlanRepository repository;

    public ProductionPlanServiceImpl(ProductionPlanRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addPlan(ProductionPlan plan) {
        repository.add(plan);
    }

    @Override
    public void updatePlan(ProductionPlan plan) {
        repository.update(plan);
    }

    @Override
    public void deletePlan(int planId) {
        repository.deleteById(planId);
    }

    @Override
    public ProductionPlan getPlanById(int planId) {
        return repository.findById(planId);
    }

    @Override
    public List<ProductionPlan> getAllPlans() {
        return repository.findAll();
    }

    @Override
    public List<WeeklyPlanDto> searchWeeklyPlans(String line, String model, Integer weekNo, Integer year) {
        return repository.searchWeeklyPlans(line, model, weekNo, year);
    }

    @Override
    public boolean createWeeklyPlan(String lineName, List<SelectedModelDto> modelList, LocalDate fromDate, LocalDate toDate, int weekNo, int year) {
        return repository.createWeeklyPlan(lineName, modelList, fromDate, toDate, weekNo, year);
    }


}
