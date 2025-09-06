package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Function;

public class TableCellUtils {

    /**
     * Gộp ô trùng bằng cách ẩn nội dung nếu giống dòng trước (không xét group).
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>>
    mergeIdenticalCells(Function<T, String> extractor) {
        return mergeIdenticalCells(extractor, null);
    }

    /**
     * Gộp ô trùng theo nhóm: CHỈ ẩn nội dung khi:
     *  - cùng group với dòng trước (ví dụ: cùng line) VÀ
     *  - giá trị của cột giống dòng trước.
     *
     * @param extractor      Lấy giá trị text của cột (vd: DailyPlanDisplayRow::getModel)
     * @param groupExtractor Lấy giá trị group (vd: DailyPlanDisplayRow::getLine). Nếu null, hành vi như bản cũ.
     */
    public static <T> Callback<TableColumn<T, String>, TableCell<T, String>>
    mergeIdenticalCells(Function<T, String> extractor, Function<T, String> groupExtractor) {

        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // reset state
                setText(null);
                setGraphic(null);

                if (empty || item == null) return;

                int rowIndex = getIndex();
                List<T> items = getTableView().getItems();
                if (rowIndex <= 0 || rowIndex >= items.size()) {
                    setText(item);
                    return;
                }

                T current = items.get(rowIndex);
                T previous = items.get(rowIndex - 1);

                String curVal = safe(extractor.apply(current));
                String prevVal = safe(extractor.apply(previous));

                // Nếu có groupExtractor, chỉ merge khi group giống nhau
                if (groupExtractor != null) {
                    String curGroup = safe(groupExtractor.apply(current));
                    String prevGroup = safe(groupExtractor.apply(previous));
                    if (!curGroup.equals(prevGroup)) {
                        // khác line -> KHÔNG merge
                        setText(item);
                        return;
                    }
                }

                // Cùng group (hoặc không dùng group) -> xét giá trị
                if (curVal.equals(prevVal)) {
                    setText(""); // ẩn nếu trùng giá trị với dòng trước trong cùng group
                } else {
                    setText(item);
                }
            }

            private String safe(String s) { return s == null ? "" : s; }
        };
    }
}
