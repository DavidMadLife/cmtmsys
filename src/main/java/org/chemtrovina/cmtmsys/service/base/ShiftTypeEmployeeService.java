package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;

import java.util.List;

public interface ShiftTypeEmployeeService {

    List<ShiftTypeEmployee> getAll();

    ShiftTypeEmployee getByCode(String code);

    void create(ShiftTypeEmployee type);

    void update(ShiftTypeEmployee type);

    void delete(String shiftCode);
}
