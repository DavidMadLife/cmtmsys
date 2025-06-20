package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ShiftChem;

import java.util.List;

public interface ShiftChemService {
    void addShift(ShiftChem shift);
    void updateShift(ShiftChem shift);
    void deleteShiftById(int shiftId);
    ShiftChem getShiftById(int shiftId);
    List<ShiftChem> getAllShifts();
}
