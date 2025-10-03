package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.Solder;
import org.chemtrovina.cmtmsys.model.SolderSession;
import org.chemtrovina.cmtmsys.repository.base.SolderSessionRepository;
import org.chemtrovina.cmtmsys.service.base.SolderService;
import org.chemtrovina.cmtmsys.service.base.SolderSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SolderSessionServiceImpl implements SolderSessionService {

    private static final int DEFAULT_AGING_MINUTES = 120;  // 2h
    private static final int DEFAULT_SCRAP_LIMIT_HOURS = 24;

    private final SolderSessionRepository sessionRepo;
    private final SolderService solderService;

    public SolderSessionServiceImpl(SolderSessionRepository sessionRepo,
                                    SolderService solderService) {
        this.sessionRepo = sessionRepo;
        this.solderService = solderService;
    }

    // -------------------- NGHIỆP VỤ CHÍNH --------------------
    @Override
    @Transactional
    public void scanOut(String code) {
        scanOut(code, DEFAULT_AGING_MINUTES, null);
    }

    @Override
    @Transactional
    public void scanOut(String code, int minAgingMinutes, String note) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Code không được trống.");
        if (minAgingMinutes <= 0)
            throw new IllegalArgumentException("minAgingMinutes phải > 0.");

        Solder solder = requireSolder(code);
        int solderId = solder.getSolderId();

        // Không cho mở phiên mới nếu đang có phiên active
        if (sessionRepo.existsActiveBySolderId(solderId)) {
            throw new IllegalStateException("Hũ đang có phiên chưa kết thúc.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate outDate = now.toLocalDate();
        LocalDateTime agingStart = now;
        LocalDateTime agingEnd = now.plusMinutes(minAgingMinutes);

        sessionRepo.createOutSession(solderId, outDate, agingStart, agingEnd, note);
    }

    @Override
    @Transactional
    public void receive(String code, Integer warehouseId, int employeeId) {
        SolderSession active = requireActiveSession(code);
        sessionRepo.markReceive(active.getSessionId(), warehouseId, employeeId, nowUtc());
    }

    @Override
    @Transactional
    public void returnSolder(String code, int employeeId) {
        SolderSession active = requireActiveSession(code);
        sessionRepo.markReturnOK(active.getSessionId(), employeeId, nowUtc());
    }

    @Override
    @Transactional
    public int autoScrapOverdue(int limitHours) {
        if (limitHours <= 0) limitHours = DEFAULT_SCRAP_LIMIT_HOURS;
        return sessionRepo.autoScrapOverdue(limitHours, nowUtc());
    }

    // -------------------- TRUY VẤN --------------------
    @Override
    public SolderSession getActiveByCode(String code) {
        Solder solder = solderService.getSolderByCode(code);
        if (solder == null) return null;
        return sessionRepo.findActiveBySolderId(solder.getSolderId());
    }

    @Override
    public List<SolderSession> getBySolderCode(String code) {
        Solder solder = solderService.getSolderByCode(code);
        if (solder == null) return List.of();
        return sessionRepo.findBySolderId(solder.getSolderId());
    }

    @Override
    public SolderSession getById(int sessionId) {
        return sessionRepo.findById(sessionId);
    }

    @Override
    public List<SolderSession> getAll() {
        return sessionRepo.findAll();
    }

    // -------------------- HELPERS --------------------
    private Solder requireSolder(String code) {
        Solder s = solderService.getSolderByCode(code);
        if (s == null) throw new IllegalArgumentException("Không tìm thấy Solder với code: " + code);
        return s;
    }

    private SolderSession requireActiveSession(String code) {
        Solder solder = requireSolder(code);
        SolderSession active = sessionRepo.findActiveBySolderId(solder.getSolderId());
        if (active == null) {
            throw new IllegalStateException("Không có phiên active cho hũ: " + code + ". Hãy scan OUT trước.");
        }
        return active;
    }

    /** Đồng bộ UTC với CreatedAt của DB (SYSUTCDATETIME). */
    private static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public void deleteSession(int sessionId) {
        sessionRepo.deleteById(sessionId);
    }

    // service.Impl.SolderSessionServiceImpl
    @Override
    public void updateSession(int sessionId, SolderSessionUpdate update) {
        sessionRepo.updatePartial(sessionId, update);
    }


}
