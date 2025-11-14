package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.List;
import java.util.stream.Collectors;

public class FxClipboardUtils {

    /**
     * 📋 Copy selected cells from TableView to clipboard as tab-separated text.
     */
    public static <T> void copySelectionToClipboard(TableView<T> table) {
        StringBuilder clipboardString = new StringBuilder();

        var selectedCells = table.getSelectionModel().getSelectedCells();
        int previousRow = -1;

        for (TablePosition<?, ?> position : selectedCells) {
            int row = position.getRow();
            Object cell = position.getTableColumn().getCellData(row);

            if (previousRow == row) {
                clipboardString.append('\t');
            } else if (previousRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell == null ? "" : cell.toString());
            previousRow = row;
        }

        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * 🪄 Enable Ctrl+C copy shortcut for any TableView.
     */
    public static <T> void enableCopyShortcut(TableView<T> table) {
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copySelectionToClipboard(table);
                event.consume();
            }
        });
    }
}
