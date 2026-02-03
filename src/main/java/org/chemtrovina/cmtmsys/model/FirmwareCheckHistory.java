package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;

public class FirmwareCheckHistory {
    private int id;
    private String inputVersion;
    private String popupVersion;
    private String result;      // OK/NG/NOT_FOUND/ERROR
    private String message;
    private LocalDateTime createdAt;

    public FirmwareCheckHistory() {}

    public FirmwareCheckHistory(int id, String inputVersion, String popupVersion, String result, String message, LocalDateTime createdAt) {
        this.id = id;
        this.inputVersion = inputVersion;
        this.popupVersion = popupVersion;
        this.result = result;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInputVersion() { return inputVersion; }
    public void setInputVersion(String inputVersion) { this.inputVersion = inputVersion; }

    public String getPopupVersion() { return popupVersion; }
    public void setPopupVersion(String popupVersion) { this.popupVersion = popupVersion; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
