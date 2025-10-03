package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SolderSession {
    private int sessionId;
    private int solderId;

    private LocalDate outDate;
    private LocalDateTime agingStartTime;
    private LocalDateTime agingEndTime;

    private Integer warehouseId;          // có thể null
    private Integer receiverEmployeeId;   // có thể null
    private LocalDateTime openTime;       // có thể null

    private LocalDateTime returnTime;     // có thể null
    private Integer returnEmployeeId;     // có thể null
    private LocalDateTime scrapTime;      // có thể null
    private String returnStatus;          // "OK" | "SCRAP" | null

    private String note;                  // có thể null
    private LocalDateTime createdAt;      // DB default

    public SolderSession() {}

    // getters/setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getSolderId() { return solderId; }
    public void setSolderId(int solderId) { this.solderId = solderId; }

    public LocalDate getOutDate() { return outDate; }
    public void setOutDate(LocalDate outDate) { this.outDate = outDate; }

    public LocalDateTime getAgingStartTime() { return agingStartTime; }
    public void setAgingStartTime(LocalDateTime agingStartTime) { this.agingStartTime = agingStartTime; }

    public LocalDateTime getAgingEndTime() { return agingEndTime; }
    public void setAgingEndTime(LocalDateTime agingEndTime) { this.agingEndTime = agingEndTime; }

    public Integer getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }

    public Integer getReceiverEmployeeId() { return receiverEmployeeId; }
    public void setReceiverEmployeeId(Integer receiverEmployeeId) { this.receiverEmployeeId = receiverEmployeeId; }

    public LocalDateTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalDateTime openTime) { this.openTime = openTime; }

    public LocalDateTime getReturnTime() { return returnTime; }
    public void setReturnTime(LocalDateTime returnTime) { this.returnTime = returnTime; }

    public Integer getReturnEmployeeId() { return returnEmployeeId; }
    public void setReturnEmployeeId(Integer returnEmployeeId) { this.returnEmployeeId = returnEmployeeId; }

    public LocalDateTime getScrapTime() { return scrapTime; }
    public void setScrapTime(LocalDateTime scrapTime) { this.scrapTime = scrapTime; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
