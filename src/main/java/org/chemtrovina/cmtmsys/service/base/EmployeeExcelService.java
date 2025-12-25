package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.EmployeeExcelDto;

import java.io.File;
import java.util.List;

public interface EmployeeExcelService
{
    List<EmployeeExcelDto> readEmployeeExcel(File file);
}
