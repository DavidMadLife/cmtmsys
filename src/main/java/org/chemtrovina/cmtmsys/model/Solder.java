package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Solder {
    private int solderId;
    private String code;
    private String maker;
    private String lot;
    private LocalDate receivedDate;
    private LocalDate mfgDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;

    // NEW: giá trị đo viscotester (đơn vị cP), cho phép null
    private Double viscotester;

    public Solder() { }

    // Constructor đầy đủ (giữ bản cũ + viscotester)
    public Solder(int solderId, String code, String maker, String lot,
                  LocalDate receivedDate, LocalDate mfgDate, LocalDate expiryDate,
                  LocalDateTime createdAt, Double viscotester) {
        this.solderId = solderId;
        this.code = code;
        this.maker = maker;
        this.lot = lot;
        this.receivedDate = receivedDate;
        this.mfgDate = mfgDate;
        this.expiryDate = expiryDate;
        this.createdAt = createdAt;
        this.viscotester = viscotester;
    }

    // Constructor tối thiểu (giữ nguyên), nếu cần thêm overload có viscotester:
    public Solder(String code, String maker, String lot, LocalDate receivedDate) {
        this.code = code;
        this.maker = maker;
        this.lot = lot;
        this.receivedDate = receivedDate;
    }

    // Getters & Setters
    public int getSolderId() { return solderId; }
    public void setSolderId(int solderId) { this.solderId = solderId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMaker() { return maker; }
    public void setMaker(String maker) { this.maker = maker; }

    public String getLot() { return lot; }
    public void setLot(String lot) { this.lot = lot; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public LocalDate getMfgDate() { return mfgDate; }
    public void setMfgDate(LocalDate mfgDate) { this.mfgDate = mfgDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // NEW
    public Double getViscotester() { return viscotester; }
    public void setViscotester(Double viscotester) { this.viscotester = viscotester; }

    // Helpers
    public boolean isExpired(LocalDate today) {
        return expiryDate != null && today != null && expiryDate.isBefore(today);
    }

    @Override
    public String toString() {
        return "Solder{" +
                "solderId=" + solderId +
                ", code='" + code + '\'' +
                ", maker='" + maker + '\'' +
                ", lot='" + lot + '\'' +
                ", receivedDate=" + receivedDate +
                ", mfgDate=" + mfgDate +
                ", expiryDate=" + expiryDate +
                ", createdAt=" + createdAt +
                ", viscotester=" + viscotester +
                '}';
    }
}
