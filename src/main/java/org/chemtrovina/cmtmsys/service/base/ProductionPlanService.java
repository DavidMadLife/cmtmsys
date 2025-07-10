package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.ProductionPlan;

import java.time.LocalDate;
import java.util.List;

public interface ProductionPlanService {
    void addPlan(ProductionPlan plan);
    void updatePlan(ProductionPlan plan);
    void deletePlan(int planId);
    ProductionPlan getPlanById(int planId);
    List<ProductionPlan> getAllPlans();
    List<WeeklyPlanDto> searchWeeklyPlans(String line, String model, Integer weekNo, Integer year);

    boolean createWeeklyPlan(String lineName, List<SelectedModelDto> modelList,
                             LocalDate fromDate, LocalDate toDate, int weekNo, int year);
    ;


}
