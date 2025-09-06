package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;

import java.time.LocalDate;
import java.util.List;

public interface ProductionPlanHourlyService {

    void saveHourlyPlan(ProductionPlanHourly plan);

    void updatePlanQty(int id, int newPlanQty);

    void updateActualQty(int id, int newActualQty);

    List<ProductionPlanHourly> getByDailyId(int dailyId);

    void deleteByDailyId(int dailyId);

    List<ProductionPlanHourly> getHourlyPlansByDailyId(int dailyId);
    List<ProductionPlanHourly> getHourlyPlansByDateAndLine(LocalDate date, String line);
    void saveOrUpdate(ProductionPlanHourly plan);
    void saveOrUpdateBulk(List<ProductionPlanHourly> plans);
}
