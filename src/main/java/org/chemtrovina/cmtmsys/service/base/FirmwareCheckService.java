package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.FirmwareCheckResultDto;
import org.chemtrovina.cmtmsys.model.FirmwareCheckHistory;

import java.util.List;

public interface FirmwareCheckService {
    FirmwareCheckResultDto checkAndSave(String inputVersion);
    List<FirmwareCheckHistory> getLatestHistory(int top);

    FirmwareCheckResultDto checkAndSave(String inputVersion, com.sun.jna.platform.win32.WinDef.HWND hwnd);

}
