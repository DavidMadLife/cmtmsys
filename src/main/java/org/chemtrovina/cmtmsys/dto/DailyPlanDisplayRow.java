package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;

import java.util.List;

public class DailyPlanDisplayRow {
    private final int planItemId;
    private final StringProperty line;
    private final StringProperty model;
    private final StringProperty productCode;
    private final StringProperty type; // "Plan", "Actual", "Diff"
    private final IntegerProperty stock;
    private final List<IntegerProperty> days;
    private final IntegerProperty total;

    private final StringProperty modelType = new SimpleStringProperty();
    private final DoubleProperty completionRate = new SimpleDoubleProperty(0);

    public DailyPlanDisplayRow(int planItemId, String line, String model, String productCode, String type, int stock,
                               int d1, int d2, int d3, int d4, int d5, int d6, int d7) {
        this.planItemId = planItemId;
        this.line = new SimpleStringProperty(line);
        this.model = new SimpleStringProperty(model);
        this.productCode = new SimpleStringProperty(productCode);
        this.type = new SimpleStringProperty(type);
        this.stock = new SimpleIntegerProperty(stock);
        this.days = List.of(
                new SimpleIntegerProperty(d1),
                new SimpleIntegerProperty(d2),
                new SimpleIntegerProperty(d3),
                new SimpleIntegerProperty(d4),
                new SimpleIntegerProperty(d5),
                new SimpleIntegerProperty(d6),
                new SimpleIntegerProperty(d7)
        );
        this.total = new SimpleIntegerProperty(d1 + d2 + d3 + d4 + d5 + d6 + d7);
    }

    public int getPlanItemId() {
        return planItemId;
    }

    public String getLine() {
        return line.get();
    }

    public String getModel() {
        return model.get();
    }

    public String getProductCode() {
        return productCode.get();
    }

    public String getType() {
        return type.get();
    }

    public int getStock() {
        return stock.get();
    }

    public int getDay(int index) {
        return days.get(index).get();
    }

    public int getTotal() {
        return total.get();
    }

    public IntegerProperty dayProperty(int index) {
        return days.get(index);
    }

    public IntegerProperty totalProperty() {
        return total;
    }

    public StringProperty lineProperty() {
        return line;
    }

    public StringProperty modelProperty() {
        return model;
    }

    public StringProperty productCodeProperty() {
        return productCode;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    // 🔽 Model Type
    public String getModelType() {
        return modelType.get();
    }

    public void setModelType(String type) {
        this.modelType.set(type);
    }

    public StringProperty modelTypeProperty() {
        return modelType;
    }

    // 🔽 Completion Rate
    public double getCompletionRate() {
        return completionRate.get();
    }

    public void setCompletionRate(double rate) {
        this.completionRate.set(rate);
    }

    public DoubleProperty completionRateProperty() {
        return completionRate;
    }

    public String getGroupKey() {
        return line.get() + "|" + model.get() + "|" + productCode.get();
    }
}
