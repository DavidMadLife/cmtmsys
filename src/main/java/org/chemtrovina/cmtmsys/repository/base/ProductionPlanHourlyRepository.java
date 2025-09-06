package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;

import java.time.LocalDate;
import java.util.List;

public interface ProductionPlanHourlyRepository {

    void insert(ProductionPlanHourly plan);

    void updatePlanQuantity(int id, int newPlanQty);

    void updateActualQuantity(int id, int newActualQty);

    void saveOrUpdate(ProductionPlanHourly plan);            // <-- Thêm

    void saveOrUpdateBulk(List<ProductionPlanHourly> plans); // <-- Thêm

    List<ProductionPlanHourly> findByDailyId(int dailyId);

    List<ProductionPlanHourly> findByDateAndLine(LocalDate date, String line); // <-- Thêm nếu cần load theo ngày + line

    void deleteByDailyId(int dailyId);
}
