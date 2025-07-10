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

    public WeeklyPlanDto(String line, String productCode, int weekNo, String fromDate, String toDate,
                         int plannedQty, int actualQty, int diffQty) {
        this.line = new SimpleStringProperty(line);
        this.productCode = new SimpleStringProperty(productCode);
        this.weekNo = new SimpleIntegerProperty(weekNo);
        this.fromDate = new SimpleStringProperty(fromDate);
        this.toDate = new SimpleStringProperty(toDate);
        this.plannedQty = new SimpleIntegerProperty(plannedQty);
        this.actualQty = new SimpleIntegerProperty(actualQty);
        this.diffQty = new SimpleIntegerProperty(diffQty);
    }

    public StringProperty lineProperty() { return line; }
    public StringProperty productCodeProperty() { return productCode; }
    public IntegerProperty weekNoProperty() { return weekNo; }
    public StringProperty fromDateProperty() { return fromDate; }
    public StringProperty toDateProperty() { return toDate; }
    public IntegerProperty plannedQtyProperty() { return plannedQty; }
    public IntegerProperty actualQtyProperty() { return actualQty; }
    public IntegerProperty diffQtyProperty() { return diffQty; }
}
