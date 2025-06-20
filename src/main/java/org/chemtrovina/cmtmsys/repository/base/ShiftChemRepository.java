package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ShiftChem;

import java.util.List;

public interface ShiftChemRepository {
    void add(ShiftChem shift);
    void update(ShiftChem shift);
    void deleteById(int shiftId);
    ShiftChem findById(int shiftId);
    List<ShiftChem> findAll();
}
