package org.chemtrovina.cmtmsys.service.Impl;

import com.sun.jna.platform.win32.WinDef;
import org.chemtrovina.cmtmsys.dto.FirmwareCheckResultDto;
import org.chemtrovina.cmtmsys.model.FirmwareCheckHistory;
import org.chemtrovina.cmtmsys.repository.base.FirmwareCheckHistoryRepository;
import org.chemtrovina.cmtmsys.service.base.FirmwareCheckService;
import org.chemtrovina.cmtmsys.utils.WindowsPopupFirmwareReader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirmwareCheckServiceImpl implements FirmwareCheckService {

    private final FirmwareCheckHistoryRepository historyRepo;

    // Filter: chỉ popup đúng yêu cầu
    private static final String TITLE_KEY = "AitUVCExtTest";
    private static final String TEXT_KEY  = "Firmware Version";

    public FirmwareCheckServiceImpl(FirmwareCheckHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    // ==========================================
    // Manual check (bấm nút Check)
    // - scan đúng popup theo rule: title OR text
    // ==========================================
    @Override
    public FirmwareCheckResultDto checkAndSave(String inputVersion) {

        String input = inputVersion == null ? "" : inputVersion.trim();

        // Tìm popup hợp lệ (title AitUVCExtTest hoặc có text Firmware Version)
        WinDef.HWND hwnd = WindowsPopupFirmwareReader.findFirstTargetPopup(TITLE_KEY, TEXT_KEY);

        // đọc version từ đúng popup
        String popup = (hwnd == null) ? null : WindowsPopupFirmwareReader.tryReadFirmwareVersion(hwnd);

        String result;
        String message;

        if (input.isBlank()) {
            result = "ERROR";
            message = "Input version is empty.";
        } else if (hwnd == null) {
            result = "NOT_FOUND";
            message = "Target popup not found (Title contains '" + TITLE_KEY + "' OR contains text '" + TEXT_KEY + "').";
        } else if (popup == null || popup.isBlank()) {
            result = "NOT_FOUND";
            message = "Popup found but cannot read firmware version.";
        } else if (popup.equalsIgnoreCase(input)) {
            result = "OK";
            message = "Version matched.";
        } else {
            result = "NG";
            message = "Version mismatch. Popup=" + popup + ", Input=" + input;
        }

        saveHistory(input, popup, result, message);

        return new FirmwareCheckResultDto(input, popup, result, message);
    }

    // ==========================================
    // Auto check (AUTO mode) - đã có hwnd
    // - KHÔNG scan lại
    // - validate hwnd có đúng popup rule không
    // ==========================================
    @Override
    public FirmwareCheckResultDto checkAndSave(String inputVersion, WinDef.HWND hwnd) {

        String input = inputVersion == null ? "" : inputVersion.trim();

        String result;
        String message;
        String popup = null;

        if (input.isBlank()) {
            result = "ERROR";
            message = "Input version is empty.";
            saveHistory(input, null, result, message);
            return new FirmwareCheckResultDto(input, null, result, message);
        }

        if (hwnd == null) {
            result = "NOT_FOUND";
            message = "Target popup handle is null.";
            saveHistory(input, null, result, message);
            return new FirmwareCheckResultDto(input, null, result, message);
        }

        // ✅ Chỉ xử lý nếu đúng popup theo rule
        boolean okTarget = WindowsPopupFirmwareReader.isTargetPopup(hwnd, TITLE_KEY, TEXT_KEY);
        if (!okTarget) {
            result = "NOT_READY";
            message = "Window is not target popup (skip).";
            // không cần lưu history nếu bạn muốn (nhưng mình vẫn lưu để debug)
            saveHistory(input, null, result, message);
            return new FirmwareCheckResultDto(input, null, result, message);
        }

        popup = WindowsPopupFirmwareReader.tryReadFirmwareVersion(hwnd);

        if (popup == null || popup.isBlank()) {
            result = "NOT_FOUND";
            message = "Cannot read firmware version from target popup.";
        } else if (popup.equalsIgnoreCase(input)) {
            result = "OK";
            message = "Version matched.";
        } else {
            result = "NG";
            message = "Version mismatch. Popup=" + popup + ", Input=" + input;
        }

        saveHistory(input, popup, result, message);

        return new FirmwareCheckResultDto(input, popup, result, message);
    }

    // ==========================================
    @Override
    public List<FirmwareCheckHistory> getLatestHistory(int top) {
        return historyRepo.findLatest(top);
    }

    // ==========================================
    private void saveHistory(String input, String popup, String result, String message) {
        FirmwareCheckHistory h = new FirmwareCheckHistory();
        h.setInputVersion(input);
        h.setPopupVersion(popup);
        h.setResult(result);
        h.setMessage(message);
        historyRepo.insert(h);
    }
}
