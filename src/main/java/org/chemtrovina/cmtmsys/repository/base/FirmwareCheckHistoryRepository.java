package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.FirmwareCheckHistory;

import java.util.List;

public interface FirmwareCheckHistoryRepository {
    void insert(FirmwareCheckHistory h);
    List<FirmwareCheckHistory> findLatest(int top);
}
