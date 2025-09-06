package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductionPlanDailyRepository {
    void add(ProductionPlanDaily daily);
    void update(ProductionPlanDaily daily);
    void deleteById(int dailyId);
    List<ProductionPlanDaily> findByPlanItemId(int planItemId);
    List<ProductionPlanDaily> findByPlanItemIdAndDateRange(int planItemId, LocalDate startDate, LocalDate endDate);

    List<DailyPlanRowDto> getDailyPlansByLineAndWeek(String lineName, int weekNo, int year);
    void updateDailyPlanAndActual(int planItemId, String runDate, int planQty, int actualQty);
    int getTotalDailyPlanByPlanItemId(int planItemId);
    int getPlannedWeeklyQuantityByPlanItemId(int planItemId);

    void updateDailyPlan(int planItemId, LocalDate runDate, int planQty);

    void updateActual(int planItemId, LocalDate runDate, int actualQty);

    List<DailyPlanRowDto> getDailyPlanViewForAllLines(int week, int year);

    void consumeMaterialByActual(int planItemId, LocalDate runDate, int actualQty);

    int getActualQuantity(int planItemId, LocalDate runDate);
    ProductionPlanDaily findByPlanItemIdAndRunDate(int planItemId, LocalDate runDate);
    ProductionPlanDaily findByModelCodeAndLineAndDate(String modelCode, String lineName, LocalDate runDate);


    Map<String, ProductionPlanDaily> findByModelLineAndDates(Set<String> keys);



}
