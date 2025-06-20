package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ShiftChem;
import org.chemtrovina.cmtmsys.repository.base.ShiftChemRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftChemService;

import java.util.List;

public class ShiftChemServiceImpl implements ShiftChemService {

    private final ShiftChemRepository shiftChemRepository;

    public ShiftChemServiceImpl(ShiftChemRepository shiftChemRepository) {
        this.shiftChemRepository = shiftChemRepository;
    }

    @Override
    public void addShift(ShiftChem shift) {
        shiftChemRepository.add(shift);
    }

    @Override
    public void updateShift(ShiftChem shift) {
        shiftChemRepository.update(shift);
    }

    @Override
    public void deleteShiftById(int shiftId) {
        shiftChemRepository.deleteById(shiftId);
    }

    @Override
    public ShiftChem getShiftById(int shiftId) {
        return shiftChemRepository.findById(shiftId);
    }

    @Override
    public List<ShiftChem> getAllShifts() {
        return shiftChemRepository.findAll();
    }
}
