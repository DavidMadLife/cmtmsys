package org.chemtrovina.cmtmsys.utils;

import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FxFilterUtils {

    public static <T> void setupFilterMenu(
            TableColumn<T, String> column,
            List<T> data,
            Function<T, String> valueExtractor,
            Consumer<List<String>> onApplyFilter
    ) {
        ContextMenu filterMenu = new ContextMenu();
        Map<String, CheckBox> checkMap = new HashMap<>();

        // Ô tìm kiếm
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm kiếm...");
        CustomMenuItem searchItem = new CustomMenuItem(searchField);
        searchItem.setHideOnClick(false);

        // Danh sách checkboxes
        VBox checkboxContainer = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(checkboxContainer);
        scrollPane.setPrefHeight(200); // ✅ Chiều cao tối đa
        scrollPane.setFitToWidth(true);

        CustomMenuItem scrollItem = new CustomMenuItem(scrollPane);
        scrollItem.setHideOnClick(false);

        // Tạo checkbox
        Set<String> values = data.stream()
                .map(valueExtractor)
                .filter(val -> val != null && !val.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));

        for (String val : values) {
            CheckBox cb = new CheckBox(val);
            cb.setSelected(true);
            checkMap.put(val, cb);
            checkboxContainer.getChildren().add(cb);
        }

        // Tìm kiếm
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            checkboxContainer.getChildren().clear();
            values.stream()
                    .filter(v -> v.toLowerCase().contains(newVal.toLowerCase()))
                    .forEach(v -> checkboxContainer.getChildren().add(checkMap.get(v)));
        });

        // Nút chọn tất cả
        Button btnSelectAll = new Button("✓ Chọn tất cả");
        btnSelectAll.setMaxWidth(Double.MAX_VALUE);
        btnSelectAll.setOnAction(e -> checkMap.values().forEach(cb -> cb.setSelected(true)));
        CustomMenuItem selectAllItem = new CustomMenuItem(btnSelectAll);
        selectAllItem.setHideOnClick(false);

        // Nút bỏ chọn tất cả
        Button btnDeselectAll = new Button("✗ Bỏ chọn tất cả");
        btnDeselectAll.setMaxWidth(Double.MAX_VALUE);
        btnDeselectAll.setOnAction(e -> checkMap.values().forEach(cb -> cb.setSelected(false)));
        CustomMenuItem deselectAllItem = new CustomMenuItem(btnDeselectAll);
        deselectAllItem.setHideOnClick(false);

        // Áp dụng
        MenuItem applyItem = new MenuItem("✓ Áp dụng lọc");
        applyItem.setStyle("-fx-font-weight: bold;");
        applyItem.setOnAction(e -> {
            List<String> selected = checkMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .toList();
            onApplyFilter.accept(selected);
            filterMenu.hide();
        });

        // Thêm vào menu
        filterMenu.getItems().addAll(
                searchItem,
                selectAllItem,
                deselectAllItem,
                new SeparatorMenuItem(),
                scrollItem,
                new SeparatorMenuItem(),
                applyItem
        );

        column.setContextMenu(filterMenu);
    }

}
