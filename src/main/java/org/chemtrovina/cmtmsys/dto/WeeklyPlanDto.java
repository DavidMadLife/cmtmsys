package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;

public class WeeklyPlanDto {

    private final StringProperty line;
    private final StringProperty productCode;
    private final IntegerProperty weekNo;
    private final StringProperty fromDate;
    private final StringProperty toDate;
    private final IntegerProperty plannedQty;
    private final IntegerProperty actualQty;
    private final IntegerProperty diffQty;

    private final StringProperty modelType;
    private final DoubleProperty completionRate;

    public WeeklyPlanDto(String line, String productCode, int weekNo, String fromDate, String toDate,
                         int plannedQty, int actualQty, int diffQty, String modelType) {
        this.line = new SimpleStringProperty(line);
        this.productCode = new SimpleStringProperty(productCode);
        this.weekNo = new SimpleIntegerProperty(weekNo);
        this.fromDate = new SimpleStringProperty(fromDate);
        this.toDate = new SimpleStringProperty(toDate);
        this.plannedQty = new SimpleIntegerProperty(plannedQty);
        this.actualQty = new SimpleIntegerProperty(actualQty);
        this.diffQty = new SimpleIntegerProperty(diffQty);
        this.modelType = new SimpleStringProperty(modelType);

        double rate = (plannedQty == 0) ? 0.0 : (actualQty * 100.0 / plannedQty);
        this.completionRate = new SimpleDoubleProperty(Math.round(rate * 10) / 10.0); // làm tròn 1 chữ số
    }

    // Properties
    public StringProperty lineProperty() { return line; }
    public StringProperty productCodeProperty() { return productCode; }
    public IntegerProperty weekNoProperty() { return weekNo; }
    public StringProperty fromDateProperty() { return fromDate; }
    public StringProperty toDateProperty() { return toDate; }
    public IntegerProperty plannedQtyProperty() { return plannedQty; }
    public IntegerProperty actualQtyProperty() { return actualQty; }
    public IntegerProperty diffQtyProperty() { return diffQty; }
    public StringProperty modelTypeProperty() { return modelType; }
    public DoubleProperty completionRateProperty() { return completionRate; }

    // Optional getters
    public String getLine() { return line.get(); }
    public String getProductCode() { return productCode.get(); }
    public int getWeekNo() { return weekNo.get(); }
    public String getFromDate() { return fromDate.get(); }
    public String getToDate() { return toDate.get(); }
    public int getPlannedQty() { return plannedQty.get(); }
    public int getActualQty() { return actualQty.get(); }
    public int getDiffQty() { return diffQty.get(); }
    public String getModelType() { return modelType.get(); }
    public double getCompletionRate() { return completionRate.get(); }

    public void setCompletionRate(double completionRate) { this.completionRate.set(completionRate); }
}
