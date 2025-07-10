package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;

public class SelectedModelDto {
    private final StringProperty modelCode = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    public SelectedModelDto(String modelCode, int quantity) {
        this.modelCode.set(modelCode);
        this.quantity.set(quantity);
    }

    public StringProperty modelCodeProperty() { return modelCode; }
    public IntegerProperty quantityProperty() { return quantity; }

    public String getModelCode() { return modelCode.get(); }
    public int getQuantity() { return quantity.get(); }
}
