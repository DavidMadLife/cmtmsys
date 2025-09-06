package org.chemtrovina.cmtmsys.utils;

import javafx.beans.property.*;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

public class TableColumnUtils {

    public static <S> void setStringColumn(TableColumn<S, String> column, Function<S, String> extractor) {
        column.setCellValueFactory(cell -> new SimpleStringProperty(extractor.apply(cell.getValue())));
    }

    public static <S> void setIntegerColumn(TableColumn<S, Number> column, Function<S, Integer> extractor) {
        column.setCellValueFactory(cell -> new SimpleIntegerProperty(extractor.apply(cell.getValue())));
    }

    public static <S, T> void setObjectColumn(TableColumn<S, T> column, Function<S, T> extractor) {
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(extractor.apply(cell.getValue())));
    }
}
