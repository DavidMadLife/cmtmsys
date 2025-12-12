package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.repository.base.ShiftTypeEmployeeRepository;
import org.chemtrovina.cmtmsys.service.base.ShiftTypeEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShiftTypeEmployeeServiceImpl implements ShiftTypeEmployeeService {

    private final ShiftTypeEmployeeRepository repo;

    public ShiftTypeEmployeeServiceImpl(ShiftTypeEmployeeRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ShiftTypeEmployee> getAll() {
        return repo.findAll();
    }

    @Override
    public ShiftTypeEmployee getByCode(String code) {
        if (code == null || code.isBlank()) return null;
        return repo.findByCode(code.trim());
    }

    @Override
    public void create(ShiftTypeEmployee type) {
        if (type == null) return;
        repo.insert(type);
    }

    @Override
    public void update(ShiftTypeEmployee type) {
        if (type == null || type.getShiftCode() == null) return;
        repo.update(type);
    }

    @Override
    public void delete(String shiftCode) {
        if (shiftCode == null || shiftCode.isBlank()) return;
        repo.delete(shiftCode.trim());
    }
}
