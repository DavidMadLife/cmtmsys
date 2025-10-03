package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.SolderSession;

import java.util.List;

public interface SolderSessionService {

    // NGHIỆP VỤ CHÍNH
    void scanOut(String code);                                  // aging mặc định 120′
    void scanOut(String code, int minAgingMinutes, String note);

    void receive(String code, Integer warehouseId, int employeeId);

    void returnSolder(String code, int employeeId);

    /** Phế các phiên có OpenTime quá limitHours mà chưa return/scrap. Trả về số dòng cập nhật. */
    int autoScrapOverdue(int limitHours);

    // TRUY VẤN – TIỆN ÍCH
    SolderSession getActiveByCode(String code);
    List<SolderSession> getBySolderCode(String code);
    SolderSession getById(int sessionId);
    List<SolderSession> getAll();

    void deleteSession(int sessionId);

    void updateSession(int sessionId, SolderSessionUpdate update);



}
