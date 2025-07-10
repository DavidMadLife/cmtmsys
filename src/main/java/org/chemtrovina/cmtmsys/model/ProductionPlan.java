package org.chemtrovina.cmtmsys.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class ProductionPlan {
    private int planID;
    private int lineWarehouseID;
    private int weekNo;
    private int year;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdAt;

    public ProductionPlan() {}

    public ProductionPlan(int planID, int lineWarehouseID, int weekNo, int year, LocalDate fromDate, LocalDate toDate, LocalDateTime createdAt) {
        this.planID = planID;
        this.lineWarehouseID = lineWarehouseID;
        this.weekNo = weekNo;
        this.year = year;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getPlanID() { return planID; }
    public void setPlanID(int planID) { this.planID = planID; }

    public int getLineWarehouseID() { return lineWarehouseID; }
    public void setLineWarehouseID(int lineWarehouseID) { this.lineWarehouseID = lineWarehouseID; }

    public int getWeekNo() { return weekNo; }
    public void setWeekNo(int weekNo) { this.weekNo = weekNo; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
