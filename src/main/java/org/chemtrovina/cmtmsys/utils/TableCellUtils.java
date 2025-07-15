package org.chemtrovina.cmtmsys.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Function;

public class TableCellUtils {

    /**
     * Gộp ô trùng bằng cách ẩn nội dung nếu giống dòng trước.
     *
     * @param extractor Hàm lấy dữ liệu từ dòng
     * @return CellFactory dùng cho cột dạng String
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>> mergeIdenticalCells(Function<T, String> extractor) {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int rowIndex = getIndex();
                    List<T> items = getTableView().getItems();
                    if (rowIndex == 0 || rowIndex >= items.size()) {
                        setText(item);
                        return;
                    }
                    T current = items.get(rowIndex);
                    T previous = items.get(rowIndex - 1);
                    if (!extractor.apply(current).equals(extractor.apply(previous))) {
                        setText(item);
                    } else {
                        setText(""); // Ẩn nếu trùng với dòng trước
                    }
                }
            }
        };
    }
}
