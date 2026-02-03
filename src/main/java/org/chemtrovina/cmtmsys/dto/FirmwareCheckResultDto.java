package org.chemtrovina.cmtmsys.dto;

public class FirmwareCheckResultDto {
    private final String inputVersion;
    private final String popupVersion;
    private final String result;
    private final String message;

    public FirmwareCheckResultDto(String inputVersion, String popupVersion, String result, String message) {
        this.inputVersion = inputVersion;
        this.popupVersion = popupVersion;
        this.result = result;
        this.message = message;
    }

    public String getInputVersion() { return inputVersion; }
    public String getPopupVersion() { return popupVersion; }
    public String getResult() { return result; }
    public String getMessage() { return message; }
}
