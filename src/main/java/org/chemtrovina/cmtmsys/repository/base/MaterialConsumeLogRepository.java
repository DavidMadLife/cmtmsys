package org.chemtrovina.cmtmsys.repository.base;

import java.time.LocalDate;

public interface MaterialConsumeLogRepository {
    boolean exists(int planItemId, LocalDate runDate);
    void insert(int planItemId, LocalDate runDate, int consumedQty);
    void delete(int planItemId, LocalDate runDate);
    int getConsumedQty(int planItemId, LocalDate runDate);

}
