package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class HourlyActualRow {
    private final int planItemId;
    private final LocalDate runDate;
    private final SimpleStringProperty line = new SimpleStringProperty();
    private final SimpleStringProperty model = new SimpleStringProperty();
    private final SimpleStringProperty productCode = new SimpleStringProperty();
    private final SimpleStringProperty modelType = new SimpleStringProperty();
    private final SimpleStringProperty aoi = new SimpleStringProperty(); // ✅ THÊM AOI
    private final IntegerProperty[] slots = new IntegerProperty[12];
    private final IntegerProperty total = new SimpleIntegerProperty(0);
    private final SimpleStringProperty stage = new SimpleStringProperty();

    public HourlyActualRow(int planItemId, String line, String model, String productCode, String modelType,
                           int[] values12, String stage, String aoi, LocalDate runDate) {
        this.planItemId = planItemId;
        this.runDate = runDate;
        this.line.set(line);
        this.model.set(model);
        this.productCode.set(productCode);
        this.modelType.set(modelType);
        this.stage.set(stage);
        this.aoi.set(aoi);
        for (int i = 0; i < 12; i++) {
            slots[i] = new SimpleIntegerProperty(values12 != null ? values12[i] : 0);
            total.set(total.get() + slots[i].get());
        }
    }

    // Getters
    public int getPlanItemId() { return planItemId; }
    public LocalDate getRunDate() {
        return runDate;
    }
    public String getLine() { return line.get(); }
    public String getModel() { return model.get(); }
    public String getProductCode() { return productCode.get(); }
    public String getModelType() { return modelType.get(); }
    public String getAoi() { return aoi.get(); } // ✅ Getter AOI
    public String getStage() { return stage.get(); }


    // Properties for TableView binding
    public SimpleStringProperty lineProperty() { return line; }
    public SimpleStringProperty modelProperty() { return model; }
    public SimpleStringProperty productCodeProperty() { return productCode; }
    public SimpleStringProperty modelTypeProperty() { return modelType; }
    public SimpleStringProperty aoiProperty() { return aoi; } // ✅ Property AOI
    public SimpleStringProperty stageProperty() { return stage; }
    public IntegerProperty slotProperty(int idx) { return slots[idx]; } // 0..11
    public IntegerProperty totalProperty() { return total; }
}
