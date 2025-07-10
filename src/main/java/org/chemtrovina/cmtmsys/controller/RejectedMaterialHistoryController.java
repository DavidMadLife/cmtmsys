package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import org.chemtrovina.cmtmsys.dto.RejectedMaterialDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.chemtrovina.cmtmsys.service.base.RejectedMaterialService;
import org.chemtrovina.cmtmsys.service.base.WorkOrderService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RejectedMaterialHistoryController {

    @FXML private TableView<RejectedMaterialDto> tblRejected;
    @FXML private TableColumn<RejectedMaterialDto, Number> colId;
    @FXML private TableColumn<RejectedMaterialDto, String> colWO;
    @FXML private TableColumn<RejectedMaterialDto, String> colSap;
    @FXML private TableColumn<RejectedMaterialDto, Number> colQty;
    @FXML private TableColumn<RejectedMaterialDto, String> colWarehouse;
    @FXML private TableColumn<RejectedMaterialDto, String> colDate;
    @FXML private TableColumn<RejectedMaterialDto, String> colNote;

    @FXML private TextField txtSearchWO;
    @FXML private TextField txtSearchSAP;
    @FXML private Button btnSearch, btnClearSearch;



    private final RejectedMaterialService rejectedMaterialService;


    private final WorkOrderService workOrderService;
    private List<RejectedMaterialDto> masterData = new ArrayList<>();


    @Autowired
    public RejectedMaterialHistoryController(RejectedMaterialService rejectedMaterialService,
                                             WorkOrderService workOrderService) {
        this.rejectedMaterialService = rejectedMaterialService;
        this.workOrderService = workOrderService;
    }


    @FXML
    public void initialize() {
        setupTable();
        loadData();

        btnSearch.setOnAction(e -> applySearch());
        btnClearSearch.setOnAction(e -> {
            txtSearchWO.clear();
            txtSearchSAP.clear();
            loadData();
        });

    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colWO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWorkOrderCode()));
        colSap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSapCode()));
        colQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        colWarehouse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarehouseName()));
        colNote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedDate().format(formatter)));


        tblRejected.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblRejected.setEditable(true); // Cho phép toàn bảng chỉnh sửa
        colNote.setCellFactory(TextFieldTableCell.forTableColumn()); // Sử dụng ô text để nhập
        colNote.setOnEditCommit(event -> {
            RejectedMaterialDto dto = event.getRowValue();
            String newNote = event.getNewValue();

            if (newNote != null && !newNote.trim().isEmpty()) {
                dto.setNote(newNote.trim());
                rejectedMaterialService.updateNoteById(dto.getId(), newNote.trim());
                showAlert("✅ Đã lưu ghi chú mới.");
            } else {
                showAlert("⚠ Ghi chú không được để trống.");
                tblRejected.refresh(); // rollback hiển thị
            }
        });

        tblRejected.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equalsIgnoreCase("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblRejected);
            }
        });

        tblRejected.getSelectionModel().setCellSelectionEnabled(true);
        tblRejected.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


    }

    private void loadData() {
        masterData = rejectedMaterialService.getAllDto();
        tblRejected.setItems(FXCollections.observableArrayList(masterData));
    }

    private void applySearch() {
        String woSearch = txtSearchWO.getText().trim().toLowerCase();
        String sapSearch = txtSearchSAP.getText().trim().toLowerCase();

        List<RejectedMaterialDto> filtered = masterData.stream()
                .filter(dto -> {
                    boolean matchWO = woSearch.isEmpty() || dto.getWorkOrderCode().toLowerCase().contains(woSearch);
                    boolean matchSAP = sapSearch.isEmpty() || dto.getSapCode().toLowerCase().contains(sapSearch);
                    return matchWO && matchSAP;
                })
                .toList();

        tblRejected.setItems(FXCollections.observableArrayList(filtered));
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
