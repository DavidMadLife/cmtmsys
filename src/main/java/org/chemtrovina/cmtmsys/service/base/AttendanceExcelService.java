package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.AttendanceImportRow;

import java.io.File;
import java.util.List;

public interface AttendanceExcelService {
    List<AttendanceImportRow> read(File file);
}
