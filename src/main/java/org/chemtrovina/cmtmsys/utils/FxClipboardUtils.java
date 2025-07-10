package org.chemtrovina.cmtmsys.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class FxClipboardUtils {

    public static void copySelectionToClipboard(TableView<?> table) {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = table.getColumns().get(col).getCellData(row);
            if (cell == null) cell = "";

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell);
            prevRow = row;
        }

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }
}
