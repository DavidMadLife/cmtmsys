package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TableUtils {

    public static void centerAlignAllColumns(TableView<?> tableView) {
        for (TableColumn<?, ?> col : tableView.getColumns()) {
            centerAlignColumn(col);
        }
    }


    public static <S, T> void centerAlignColumn(TableColumn<S, T> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.toString());
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                };
            }
        });
    }
}
