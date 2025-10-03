package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.SolderSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SolderSessionRepository {

    // CRUD thô
    void add(SolderSession session);
    void update(SolderSession session);
    void deleteById(int sessionId);

    SolderSession findById(int sessionId);
    List<SolderSession> findAll();
    List<SolderSession> findBySolderId(int solderId);

    // Tác vụ nghiệp vụ hay dùng
    boolean existsActiveBySolderId(int solderId);
    SolderSession findActiveBySolderId(int solderId);

    // Các update nhanh theo bước
    void createOutSession(int solderId, LocalDate outDate,
                          LocalDateTime agingStart, LocalDateTime agingEnd, String note);

    void markReceive(int sessionId, Integer warehouseId, Integer receiverEmployeeId, LocalDateTime openTime);

    void markReturnOK(int sessionId, Integer returnEmployeeId, LocalDateTime returnTime);

    void markScrap(int sessionId, LocalDateTime scrapTime);

    /** Phế hàng loạt: open quá limitHours mà chưa trả/chưa scrap. Trả về số dòng cập nhật. */
    int autoScrapOverdue(int limitHours, LocalDateTime now);

    void updatePartial(int sessionId, SolderSessionUpdate u);
}
