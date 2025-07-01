package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.chemtrovina.cmtmsys.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateWOController {
    @FXML
    private Label lblNewCode;
    @FXML private TableView<Product> tblProducts;
    @FXML private TableColumn<Product,String> colCode;
    @FXML private TableColumn<Product,String> colDesc;
    @FXML private TableColumn<Product,Integer> colQty;

    private Map<String,Integer> selected = new HashMap<>();
    private String workOrderCode;

    public void initData(List<Product> allProducts, String nextCode) {
        this.workOrderCode = nextCode;
        lblNewCode.setText(nextCode);
        // gán dữ liệu bảng sản phẩm và cho cột qty là textbox cell
        colCode.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getProductCode()));
        colDesc.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getDescription()));
        colQty.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQty.setOnEditCommit(ev->selected.put(
                ev.getRowValue().getProductCode(), ev.getNewValue()
        ));
        tblProducts.setItems(FXCollections.observableArrayList(allProducts));
        tblProducts.setEditable(true);
    }

    public String getWorkOrderCode() { return workOrderCode; }
    public Map<String,Integer> getSelectedItems() {
        return selected.entrySet().stream()
                .filter(e->e.getValue()>0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

