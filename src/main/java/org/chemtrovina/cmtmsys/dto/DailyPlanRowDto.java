package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;

public class DailyPlanRowDto {

    private final IntegerProperty planItemId = new SimpleIntegerProperty();

    private final StringProperty modelCode = new SimpleStringProperty();
    private final StringProperty sapCode = new SimpleStringProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty();
    private final StringProperty modelName = new SimpleStringProperty();


    private final IntegerProperty day1Plan = new SimpleIntegerProperty();
    private final IntegerProperty day2Plan = new SimpleIntegerProperty();
    private final IntegerProperty day3Plan = new SimpleIntegerProperty();
    private final IntegerProperty day4Plan = new SimpleIntegerProperty();
    private final IntegerProperty day5Plan = new SimpleIntegerProperty();
    private final IntegerProperty day6Plan = new SimpleIntegerProperty();
    private final IntegerProperty day7Plan = new SimpleIntegerProperty();

    private final IntegerProperty day1Actual = new SimpleIntegerProperty();
    private final IntegerProperty day2Actual = new SimpleIntegerProperty();
    private final IntegerProperty day3Actual = new SimpleIntegerProperty();
    private final IntegerProperty day4Actual = new SimpleIntegerProperty();
    private final IntegerProperty day5Actual = new SimpleIntegerProperty();
    private final IntegerProperty day6Actual = new SimpleIntegerProperty();
    private final IntegerProperty day7Actual = new SimpleIntegerProperty();

    private final IntegerProperty totalActual = new SimpleIntegerProperty();

    private final IntegerProperty totalDiff = new SimpleIntegerProperty();

    private final IntegerProperty totalPlan = new SimpleIntegerProperty();

    private final StringProperty modelType = new SimpleStringProperty();


    public DailyPlanRowDto(int planItemId, String modelCode, String modelName, String sapCode, int stock,
                           int d1, int d2, int d3, int d4, int d5, int d6, int d7,
                           int a1, int a2, int a3, int a4, int a5, int a6, int a7,
                           String modelType) {
        this.planItemId.set(planItemId);

        this.modelCode.set(modelCode);
        this.modelName.set(modelName);
        this.sapCode.set(sapCode);
        this.stock.set(stock);
        this.modelType.set(modelType); // 👈 Gán modelType

        this.day1Plan.set(d1);
        this.day2Plan.set(d2);
        this.day3Plan.set(d3);
        this.day4Plan.set(d4);
        this.day5Plan.set(d5);
        this.day6Plan.set(d6);
        this.day7Plan.set(d7);

        this.day1Actual.set(a1);
        this.day2Actual.set(a2);
        this.day3Actual.set(a3);
        this.day4Actual.set(a4);
        this.day5Actual.set(a5);
        this.day6Actual.set(a6);
        this.day7Actual.set(a7);

        recalculateTotals();
    }


    public void recalculateTotals() {
        int planTotal = getDay1Plan() + getDay2Plan() + getDay3Plan() + getDay4Plan() +
                getDay5Plan() + getDay6Plan() + getDay7Plan();

        int actualTotal = getDay1Actual() + getDay2Actual() + getDay3Actual() + getDay4Actual() +
                getDay5Actual() + getDay6Actual() + getDay7Actual();

        totalPlan.set(planTotal);
        totalActual.set(actualTotal);
        totalDiff.set(actualTotal - planTotal);
    }

    // === Getters/Setters/Properties ===

    // === PlanItem ID ===
    public int getPlanItemId() { return planItemId.get(); }
    public void setPlanItemId(int value) { planItemId.set(value); }
    public IntegerProperty planItemIdProperty() { return planItemId; }

    // === Model & SAP ===
    public String getModelCode() { return modelCode.get(); }
    public void setModelCode(String value) { modelCode.set(value); }
    public StringProperty modelCodeProperty() { return modelCode; }

    public String getModelName() { return modelName.get(); }
    public void setModelName(String value) { modelName.set(value); }
    public StringProperty modelNameProperty() { return modelName; }


    public String getSapCode() { return sapCode.get(); }
    public void setSapCode(String value) { sapCode.set(value); }
    public StringProperty sapCodeProperty() { return sapCode; }

    public int getStock() { return stock.get(); }
    public void setStock(int value) { stock.set(value); }
    public IntegerProperty stockProperty() { return stock; }

    // === Plan ===
    public int getDay1Plan() { return day1Plan.get(); }
    public void setDay1Plan(int value) { day1Plan.set(value); recalculateTotals(); }
    public IntegerProperty day1PlanProperty() { return day1Plan; }

    public int getDay2Plan() { return day2Plan.get(); }
    public void setDay2Plan(int value) { day2Plan.set(value); recalculateTotals(); }
    public IntegerProperty day2PlanProperty() { return day2Plan; }

    public int getDay3Plan() { return day3Plan.get(); }
    public void setDay3Plan(int value) { day3Plan.set(value); recalculateTotals(); }
    public IntegerProperty day3PlanProperty() { return day3Plan; }

    public int getDay4Plan() { return day4Plan.get(); }
    public void setDay4Plan(int value) { day4Plan.set(value); recalculateTotals(); }
    public IntegerProperty day4PlanProperty() { return day4Plan; }

    public int getDay5Plan() { return day5Plan.get(); }
    public void setDay5Plan(int value) { day5Plan.set(value); recalculateTotals(); }
    public IntegerProperty day5PlanProperty() { return day5Plan; }

    public int getDay6Plan() { return day6Plan.get(); }
    public void setDay6Plan(int value) { day6Plan.set(value); recalculateTotals(); }
    public IntegerProperty day6PlanProperty() { return day6Plan; }

    public int getDay7Plan() { return day7Plan.get(); }
    public void setDay7Plan(int value) { day7Plan.set(value); recalculateTotals(); }
    public IntegerProperty day7PlanProperty() { return day7Plan; }

    public int getTotalPlan() { return totalPlan.get(); }
    public IntegerProperty totalPlanProperty() { return totalPlan; }
    // === Actual ===
    public int getDay1Actual() { return day1Actual.get(); }
    public void setDay1Actual(int value) { day1Actual.set(value); recalculateTotals(); }
    public IntegerProperty day1ActualProperty() { return day1Actual; }

    public int getDay2Actual() { return day2Actual.get(); }
    public void setDay2Actual(int value) { day2Actual.set(value); recalculateTotals(); }
    public IntegerProperty day2ActualProperty() { return day2Actual; }

    public int getDay3Actual() { return day3Actual.get(); }
    public void setDay3Actual(int value) { day3Actual.set(value); recalculateTotals(); }
    public IntegerProperty day3ActualProperty() { return day3Actual; }

    public int getDay4Actual() { return day4Actual.get(); }
    public void setDay4Actual(int value) { day4Actual.set(value); recalculateTotals(); }
    public IntegerProperty day4ActualProperty() { return day4Actual; }

    public int getDay5Actual() { return day5Actual.get(); }
    public void setDay5Actual(int value) { day5Actual.set(value); recalculateTotals(); }
    public IntegerProperty day5ActualProperty() { return day5Actual; }

    public int getDay6Actual() { return day6Actual.get(); }
    public void setDay6Actual(int value) { day6Actual.set(value); recalculateTotals(); }
    public IntegerProperty day6ActualProperty() { return day6Actual; }

    public int getDay7Actual() { return day7Actual.get(); }
    public void setDay7Actual(int value) { day7Actual.set(value); recalculateTotals(); }
    public IntegerProperty day7ActualProperty() { return day7Actual; }

    public int getTotalActual() { return totalActual.get(); }
    public IntegerProperty totalActualProperty() { return totalActual; }

    public int getTotalDiff() { return totalDiff.get(); }
    public IntegerProperty totalDiffProperty() { return totalDiff; }

    public String getModelType() {
        return modelType.get();
    }

    public void setModelType(String value) {
        modelType.set(value);
    }

    public StringProperty modelTypeProperty() {
        return modelType;
    }


    public void setDayActual(int index, int value) {
        switch (index) {
            case 1 -> setDay1Actual(value);
            case 2 -> setDay2Actual(value);
            case 3 -> setDay3Actual(value);
            case 4 -> setDay4Actual(value);
            case 5 -> setDay5Actual(value);
            case 6 -> setDay6Actual(value);
            case 7 -> setDay7Actual(value);
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        }
    }
    public int getDayActual(int index) {
        return switch (index) {
            case 1 -> getDay1Actual();
            case 2 -> getDay2Actual();
            case 3 -> getDay3Actual();
            case 4 -> getDay4Actual();
            case 5 -> getDay5Actual();
            case 6 -> getDay6Actual();
            case 7 -> getDay7Actual();
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        };
    }


    public void setDayPlan(int index, int value) {
        switch (index) {
            case 1 -> setDay1Plan(value);
            case 2 -> setDay2Plan(value);
            case 3 -> setDay3Plan(value);
            case 4 -> setDay4Plan(value);
            case 5 -> setDay5Plan(value);
            case 6 -> setDay6Plan(value);
            case 7 -> setDay7Plan(value);
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        }
    }

    public int getDayPlan(int index) {
        return switch (index) {
            case 1 -> getDay1Plan();
            case 2 -> getDay2Plan();
            case 3 -> getDay3Plan();
            case 4 -> getDay4Plan();
            case 5 -> getDay5Plan();
            case 6 -> getDay6Plan();
            case 7 -> getDay7Plan();
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        };
    }



}
