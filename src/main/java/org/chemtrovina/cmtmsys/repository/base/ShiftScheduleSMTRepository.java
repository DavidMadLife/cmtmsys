package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import java.util.List;

public interface ShiftScheduleSMTRepository {
    void add(ShiftScheduleSMT shift);
    void update(ShiftScheduleSMT shift);
    void deleteById(int shiftId);
    ShiftScheduleSMT findById(int shiftId);
    List<ShiftScheduleSMT> findAll();
    List<ShiftScheduleSMT> findByDate(String date);
    ShiftScheduleSMT findCurrentShift(int warehouseId, java.time.LocalDateTime time);

}
