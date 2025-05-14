package org.chemtrovina.cmtmsys.utils;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

public class AutoCompleteUtils {

    public static void setupAutoComplete(TextField textField, List<String> suggestions) {
        ContextMenu suggestionsPopup = new ContextMenu();

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                suggestionsPopup.hide();
                return;
            }

            List<MenuItem> matched = suggestions.stream()
                    .filter(item -> item.toLowerCase().contains(newText.toLowerCase()))
                    .limit(10)
                    .map(item -> {
                        MenuItem menuItem = new MenuItem(item);
                        menuItem.setOnAction(e -> {
                            textField.setText(item);
                            suggestionsPopup.hide();
                        });
                        return menuItem;
                    })
                    .collect(Collectors.toList());

            if (!matched.isEmpty()) {
                suggestionsPopup.getItems().setAll(matched);
                if (!suggestionsPopup.isShowing()) {
                    suggestionsPopup.show(textField, Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionsPopup.hide();
            }
        });

        textField.focusedProperty().addListener((obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                suggestionsPopup.hide();
            }
        });
    }
}
