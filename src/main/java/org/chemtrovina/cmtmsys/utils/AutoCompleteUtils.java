package org.chemtrovina.cmtmsys.utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

public class AutoCompleteUtils {

    public static AutoCompletionBinding setupAutoComplete(TextField textField, List<String> suggestions) {
        ContextMenu suggestionsPopup = new ContextMenu();

        ChangeListener<String> textListener = (obs, oldText, newText) -> {
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
                    suggestionsPopup.show(textField, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            } else {
                suggestionsPopup.hide();
            }
        };

        ChangeListener<Boolean> focusListener = (obs, oldFocus, newFocus) -> {
            if (!newFocus) {
                suggestionsPopup.hide();
            }
        };

        textField.textProperty().addListener(textListener);
        textField.focusedProperty().addListener(focusListener);

        return new AutoCompletionBinding(() -> {
            textField.textProperty().removeListener(textListener);
            textField.focusedProperty().removeListener(focusListener);
            suggestionsPopup.hide();
        });
    }

    public static class AutoCompletionBinding {
        private final Runnable disposeAction;

        public AutoCompletionBinding(Runnable disposeAction) {
            this.disposeAction = disposeAction;
        }

        public void dispose() {
            disposeAction.run();
        }
    }
}
