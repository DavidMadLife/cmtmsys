package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.function.Function;

public class FxComboBoxUtils {

    public static <T> void setupDisplay(ComboBox<T> comboBox, Function<T, String> displayFunc) {
        comboBox.setCellFactory((ListView<T> param) -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFunc.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFunc.apply(item));
            }
        });
    }
}
