package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.DailyPlanRowDto;
import org.chemtrovina.cmtmsys.model.ProductionPlanDaily;
import org.chemtrovina.cmtmsys.model.ProductionPlanHourly;
import org.chemtrovina.cmtmsys.repository.base.MaterialConsumeLogRepository;
import org.chemtrovina.cmtmsys.repository.base.MaterialRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanDailyRepository;
import org.chemtrovina.cmtmsys.repository.base.ProductionPlanHourlyRepository;
import org.chemtrovina.cmtmsys.service.base.ProductionPlanDailyService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductionPlanDailyServiceImpl implements ProductionPlanDailyService {

    private final ProductionPlanDailyRepository repository;
    private final ProductionPlanHourlyRepository hourlyRepository;
    private final MaterialConsumeLogRepository logRepo;
    private final MaterialRepository materialRepo;
    private final JdbcTemplate jdbcTemplate;
    private final WarehouseService warehouseService;

    public ProductionPlanDailyServiceImpl(ProductionPlanDailyRepository repository,
                                          ProductionPlanHourlyRepository hourlyRepository,
                                          MaterialConsumeLogRepository logRepo,
                                          MaterialRepository materialRepo,
                                          JdbcTemplate jdbcTemplate,
                                          WarehouseService warehouseService) {
        this.repository = repository;
        this.hourlyRepository = hourlyRepository;
        this.logRepo = logRepo;
        this.materialRepo = materialRepo;
        this.jdbcTemplate = jdbcTemplate;
        this.warehouseService = warehouseService;
    }

    @Override
    @Transactional
    public void backfillActualFromPerformanceByGoodModules(String lineNameOrNull,
                                                           LocalDate weekMonday,
                                                           boolean insertMissingDaily) {
        Integer lineId = null;
        if (lineNameOrNull != null && !lineNameOrNull.isBlank() &&
                !"Tất cả".equalsIgnoreCase(lineNameOrNull)) {
            lineId = warehouseService.getIdByName(lineNameOrNull);
        }
        jdbcTemplate.update(
                "EXEC dbo.BackfillActual_GoodModules ?, ?, ?",
                java.sql.Date.valueOf(weekMonday),
                lineId,
                insertMissingDaily ? 1 : 0
        );
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
        Integer oldQty = logRepo.getLoggedQuantity(planItemId, runDate);

        if (oldQty != null) {
            if (actualQty <= oldQty) return;
            int delta = actualQty - oldQty;
            repository.consumeMaterialByActual(planItemId, runDate, delta);
            logRepo.update(planItemId, runDate, actualQty);
        } else {
            repository.consumeMaterialByActual(planItemId, runDate, actualQty);
            logRepo.insert(planItemId, runDate, actualQty);
        }
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
    public ProductionPlanDaily findByModelLineAndDate(String modelCode, String lineName, LocalDate date) {
        return repository.findByModelCodeAndLineAndDate(modelCode, lineName, date);
    }

    @Override
    public Map<String, ProductionPlanDaily> findByModelLineAndDates(Set<String> keys) {
        return repository.findByModelLineAndDates(keys);
    }

    @Override
    public boolean updateHourlyPlanWithValidation(int planItemId, int slotIndex, int newSlotQty, LocalDate runDate) {
        System.out.printf("🔍 Input runDate: %s - PlanItemId: %d - SlotIndex: %d - NewQty: %d\n",
                runDate, planItemId, slotIndex, newSlotQty);

        ProductionPlanDaily daily = repository.findByPlanItemIdAndRunDate(planItemId, runDate);
        if (daily == null) {
            System.out.println("❌ Không tìm thấy kế hoạch ngày!");
            return false;
        }

        int dailyId = daily.getDailyID();
        List<ProductionPlanHourly> slots = hourlyRepository.findByDailyId(dailyId);

        System.out.println("📦 Slots hiện tại trong DB:");
        for (ProductionPlanHourly s : slots) {
            System.out.printf("➡️ Slot %d - Qty %d\n", s.getSlotIndex(), s.getPlanQuantity());
        }

        int sumExcept = slots.stream()
                .filter(s -> s.getSlotIndex() != slotIndex)
                .mapToInt(ProductionPlanHourly::getPlanQuantity)
                .sum();

        if (sumExcept + newSlotQty > daily.getQuantity()) {
            System.out.printf("❌ Vượt: %d + %d > %d\n", sumExcept, newSlotQty, daily.getQuantity());
            return false;
        }

        ProductionPlanHourly slot = slots.stream()
                .filter(s -> s.getSlotIndex() == slotIndex)
                .findFirst().orElse(null);

        if (slot == null) {
            System.out.println("🆕 Slot chưa tồn tại ➜ tiến hành tạo mới.");
            ProductionPlanHourly hourly = new ProductionPlanHourly();
            hourly.setDailyId(dailyId);
            hourly.setSlotIndex(slotIndex);
            hourly.setRunHour(calculateSlotStartTime(slotIndex, runDate));
            hourly.setPlanQuantity(newSlotQty);
            hourly.setActualQuantity(0);
            hourlyRepository.insert(hourly);
            System.out.println("✅ Đã insert slot mới vào DB.");
        } else {
            System.out.printf("✏️ Slot đã tồn tại (ID: %d) ➜ cập nhật.\n", slot.getHourlyId());
            hourlyRepository.updatePlanQuantity(slot.getHourlyId(), newSlotQty);
        }



        return true;
    }

    @Override
    public List<ProductionPlanHourly> getHourlyPlansByDailyId(int dailyId) {
        return hourlyRepository.findByDailyId(dailyId);
    }

    private LocalDateTime calculateSlotStartTime(int slotIndex, LocalDate date) {
        int hour = (8 + slotIndex * 2) % 24;
        if (hour < 8) date = date.plusDays(1);
        return date.atTime(hour, 0);
    }
}
