package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.SelectedModelDto;
import org.chemtrovina.cmtmsys.dto.WeeklyPlanDto;
import org.chemtrovina.cmtmsys.model.ProductionPlan;

import java.time.LocalDate;
import java.util.List;

public interface ProductionPlanRepository {
    void add(ProductionPlan plan);
    void update(ProductionPlan plan);
    void deleteById(int planId);
    ProductionPlan findById(int planId);
    List<ProductionPlan> findAll();
    List<WeeklyPlanDto> searchWeeklyPlans(String line, String model, Integer weekNo, Integer year);
    public boolean createWeeklyPlan(String lineName, List<SelectedModelDto> modelList,
                                    LocalDate fromDate, LocalDate toDate, int weekNo, int year)
            ;

}
