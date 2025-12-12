package org.chemtrovina.cmtmsys.controller.inventoryTransfer;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.chemtrovina.cmtmsys.dto.SAPSummaryDto;
import org.chemtrovina.cmtmsys.dto.TransferredDto;

public class TransferTableManager {

    private final TransferScanHandler scanHandler;

    public TransferTableManager(TransferScanHandler scanHandler) {
        this.scanHandler = scanHandler;
    }

    /* ============================================================
     * REQUIRED SUMMARY TABLE
     * ============================================================ */
    public void setupRequiredSummaryTable(
            TableView<SAPSummaryDto> tblRequiredSummary,
            TableColumn<SAPSummaryDto, String> colSapCodeRequired,
            TableColumn<SAPSummaryDto, Integer> colRequired,
            TableColumn<SAPSummaryDto, Integer> colScanned,
            TableColumn<SAPSummaryDto, String> colStatus,
            TableColumn<Object, Integer> colNoRequired) {

        colNoRequired.setCellValueFactory(c ->
                new SimpleIntegerProperty(tblRequiredSummary.getItems().indexOf(c.getValue()) + 1).asObject());

        colSapCodeRequired.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSapCode()));
        colRequired.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRequired()).asObject());
        colScanned.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getScanned()).asObject());
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }


    /* ============================================================
     * TRANSFERRED TABLE
     * ============================================================ */
    public void setupTransferredTable(
            TableView<TransferredDto> tblTransferred,
            TableColumn<Object, Integer> colNoTransferred,
            TableColumn<TransferredDto, String> colBarcode,
            TableColumn<TransferredDto, String> colSapCode,
            TableColumn<TransferredDto, String> colSpec,
            TableColumn<TransferredDto, Integer> colQuantity,
            TableColumn<TransferredDto, String> colFromWarehouse,
            TableColumn<TransferredDto, String> colToWarehouse) {

        colNoTransferred.setCellValueFactory(c ->
                new SimpleIntegerProperty(tblTransferred.getItems().indexOf(c.getValue()) + 1).asObject());

        colBarcode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRollCode()));
        colSapCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSapCode()));
        colSpec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpec()));
        colQuantity.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        colFromWarehouse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFromWarehouse()));
        colToWarehouse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToWarehouse()));

        // default items
        tblTransferred.setItems(scanHandler.getTransferredMasterList());
    }


    /* ============================================================
     * SEARCH
     * ============================================================ */
    public void setupTransferredSearch(
            TableView<TransferredDto> tblTransferred,
            TextField txtSearchTransferred) {

        txtSearchTransferred.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.trim().toLowerCase();

            if (keyword.isEmpty()) {
                tblTransferred.setItems(scanHandler.getTransferredMasterList());
                return;
            }

            tblTransferred.setItems(
                    scanHandler.getTransferredMasterList().filtered(dto ->
                            dto.getRollCode().toLowerCase().contains(keyword) ||
                                    dto.getSapCode().toLowerCase().contains(keyword)
                    )
            );
        });
    }
}

