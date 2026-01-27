package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;

import java.util.List;
import java.util.Set;

public interface ShiftTypeEmployeeRepository {

    List<ShiftTypeEmployee> findAll();

    ShiftTypeEmployee findByCode(String code);

    int insert(ShiftTypeEmployee type);

    int update(ShiftTypeEmployee type);

    int delete(String shiftCode);

    List<ShiftTypeEmployee> findByCodes(Set<String> shiftCodes);
}
