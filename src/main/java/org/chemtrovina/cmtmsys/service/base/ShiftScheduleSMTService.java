package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import java.util.List;

public interface ShiftScheduleSMTService {
    void addShift(ShiftScheduleSMT shift);
    void updateShift(ShiftScheduleSMT shift);
    void deleteShiftById(int id);
    ShiftScheduleSMT getShiftById(int id);
    List<ShiftScheduleSMT> getAllShifts();
    List<ShiftScheduleSMT> getShiftsByDate(String date);
}
