package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

public class SelectedModelDto {
    private final StringProperty modelCode = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<ModelType> modelType = new SimpleObjectProperty<>();

    public SelectedModelDto(String modelCode, int quantity, ModelType modelType) {
        this.modelCode.set(modelCode);
        this.quantity.set(quantity);
        this.modelType.set(modelType);
    }

    public StringProperty modelCodeProperty() { return modelCode; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<ModelType> modelTypeProperty() { return modelType; }

    public String getModelCode() { return modelCode.get(); }
    public int getQuantity() { return quantity.get(); }
    public ModelType getModelType() { return modelType.get(); }

    public void setModelType(ModelType type) { this.modelType.set(type); }
}
