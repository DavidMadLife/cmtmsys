package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.model.StencilTransfer;
import org.chemtrovina.cmtmsys.service.base.StencilTransferService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StencilTransferController {

    // ====== FXML ======
    @FXML private TableView<StencilTransfer> tblTransfers;
    @FXML private TableColumn<StencilTransfer, Number> colSTT;
    @FXML private TableColumn<StencilTransfer, String> colBarcode;
    @FXML private TableColumn<StencilTransfer, String> colFrom;
    @FXML private TableColumn<StencilTransfer, String> colTo;
    @FXML private TableColumn<StencilTransfer, String> colPerformedBy;
    @FXML private TableColumn<StencilTransfer, String> colNote;
    @FXML private TableColumn<StencilTransfer, String> colDate;
    @FXML private Label lblTotal;
    @FXML private Button btnRefresh, btnClear;

    // ====== Services ======
    private final StencilTransferService transferService;
    private final WarehouseService warehouseService;

    private List<StencilTransfer> master;

    @Autowired
    public StencilTransferController(StencilTransferService transferService,
                                     WarehouseService warehouseService) {
        this.transferService = transferService;
        this.warehouseService = warehouseService;
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadTransfers();

        btnRefresh.setOnAction(e -> loadTransfers());
        btnClear.setOnAction(e -> {
            tblTransfers.getSelectionModel().clearSelection();
            loadTransfers();
        });
    }

    private void setupColumns() {
        colSTT.setCellValueFactory(cd ->
                Bindings.createIntegerBinding(() -> tblTransfers.getItems().indexOf(cd.getValue()) + 1));

        colBarcode.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getBarcode()));

        colFrom.setCellValueFactory(cd -> {
            Integer fromId = cd.getValue().getFromWarehouseId();
            String name = fromId == null ? "(NULL)" :
                    warehouseService.getWarehouseById(fromId) != null
                            ? warehouseService.getWarehouseById(fromId).getName()
                            : "#" + fromId;
            return new SimpleObjectProperty<>(name);
        });

        colTo.setCellValueFactory(cd -> {
            Integer toId = cd.getValue().getToWarehouseId();
            String name = toId == null ? "(NULL)" :
                    warehouseService.getWarehouseById(toId) != null
                            ? warehouseService.getWarehouseById(toId).getName()
                            : "#" + toId;
            return new SimpleObjectProperty<>(name);
        });

        colPerformedBy.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getPerformedBy()));
        colNote.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getNote()));
        colDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(
                cd.getValue().getTransferDate() == null ? "" :
                        cd.getValue().getTransferDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
    }

    private void loadTransfers() {
        master = transferService.getAllTransfers();
        tblTransfers.setItems(FXCollections.observableArrayList(master));
        lblTotal.setText(String.valueOf(master.size()));
    }
}
