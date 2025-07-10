package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.*;

import java.util.HashMap;
import java.util.Map;

public class WeeklyRunMatrixRow {
    private final StringProperty line = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty sapCode = new SimpleStringProperty();
    private final StringProperty stage = new SimpleStringProperty();
    private final IntegerProperty stock = new SimpleIntegerProperty(0);
    private final IntegerProperty total = new SimpleIntegerProperty(0);

    private final Map<String, IntegerProperty> dayValues = new HashMap<>();

    public WeeklyRunMatrixRow(String line, String model, String sapCode, String stage, int stock) {
        this.line.set(line);
        this.model.set(model);
        this.sapCode.set(sapCode);
        this.stage.set(stage);
        this.stock.set(stock);
    }

    public String getLine() { return line.get(); }
    public StringProperty lineProperty() { return line; }

    public String getModel() { return model.get(); }
    public StringProperty modelProperty() { return model; }

    public String getSapCode() { return sapCode.get(); }
    public StringProperty sapCodeProperty() { return sapCode; }

    public String getStage() { return stage.get(); }
    public StringProperty stageProperty() { return stage; }

    public int getStock() { return stock.get(); }
    public IntegerProperty stockProperty() { return stock; }

    public int getTotal() { return total.get(); }
    public IntegerProperty totalProperty() { return total; }

    public IntegerProperty getDayProperty(String day) {
        return dayValues.computeIfAbsent(day, d -> new SimpleIntegerProperty(0));
    }
}
