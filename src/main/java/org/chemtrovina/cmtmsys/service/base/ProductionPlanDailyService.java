package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductionPlanDailyService {
    void addDaily(ProductionPlanDaily daily);
    void updateDaily(ProductionPlanDaily daily);
    void deleteDaily(int dailyId);
    List<ProductionPlanDaily> getDailyByItemId(int planItemId);
    List<ProductionPlanDaily> getDailyByItemIdAndDateRange(int planItemId, LocalDate start, LocalDate end);

    List<DailyPlanRowDto> getDailyPlanView(String lineName, int weekNo, int year);
    void updateDailyPlanAndActual(int planItemId, String runDate, int planQty, int actualQty); // 💡
    int getTotalDailyPlanByPlanItemId(int planItemId);
    int getPlannedWeeklyQuantityByPlanItemId(int planItemId);
    void updateDailyPlan(int planItemId, LocalDate runDate, int planQty);
    void updateActual(int planItemId, LocalDate runDate, int actualQty);
    List<DailyPlanRowDto> getDailyPlanViewForAllLines(int week, int year);

    void consumeMaterialByActual(int planItemId, LocalDate runDate, int actualQty);

    int getActualQuantity(int planItemId, LocalDate runDate);
    void rollbackConsumeMaterial(int planItemId, LocalDate runDate);

    void backfillActualFromPerformanceByGoodModules(
            String lineNameOrNull, LocalDate weekMonday, boolean insertMissingDaily);


    boolean updateHourlyPlanWithValidation(int planItemId, int slotIndex, int newSlotQty, LocalDate runDate);
    ProductionPlanDaily findByModelLineAndDate(String modelCode, String lineName, LocalDate date);

    Map<String, ProductionPlanDaily> findByModelLineAndDates(Set<String> keys);
    // key = "model|line|date"


    List<ProductionPlanHourly> getHourlyPlansByDailyId(int dailyId);
}
