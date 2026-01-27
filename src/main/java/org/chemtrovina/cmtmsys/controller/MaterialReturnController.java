package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.model.Material;
import org.chemtrovina.cmtmsys.model.TransferLog;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.MaterialService;
import org.chemtrovina.cmtmsys.service.base.TransferLogService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class MaterialReturnController {

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtReturnQty;
    @FXML private Button btnReturn;
    @FXML private Button btnReopen;


    @FXML private TableView<WarehouseTransferDetail> tblReturnedMaterials;
    @FXML private TableColumn<WarehouseTransferDetail, Number> colNo;
    @FXML private TableColumn<WarehouseTransferDetail, String> colRollCode;
    @FXML private TableColumn<WarehouseTransferDetail, String> colSapCode;
    @FXML private TableColumn<WarehouseTransferDetail, Number> colQuantity;
    @FXML private TableColumn<WarehouseTransferDetail, String> colFromWarehouse;
    @FXML private TableColumn<WarehouseTransferDetail, String> colToWarehouse;
    @FXML private TableColumn<WarehouseTransferDetail, String> colNote;

    private final WarehouseTransferService warehouseTransferService;
    private final WarehouseService warehouseService;
    private final MaterialService materialService;
    private final TransferLogService transferLogService;

    private final ObservableList<WarehouseTransferDetail> returnedList = FXCollections.observableArrayList();

    @Autowired
    public MaterialReturnController(WarehouseTransferService warehouseTransferService,
                                    WarehouseService warehouseService,
                                    MaterialService materialService,
                                    TransferLogService transferLogService) {
        this.warehouseTransferService = warehouseTransferService;
        this.warehouseService = warehouseService;
        this.materialService = materialService;
        this.transferLogService = transferLogService;
    }

    @FXML
    public void initialize() {
        setupTable();
        btnReturn.setOnAction(e -> handleReturnMaterial());
        btnReopen.setOnAction(e -> {
            String rollCode = txtBarcode.getText().trim();
            String employeeId = txtEmployeeId.getText().trim();
            if (rollCode.isEmpty() || employeeId.isEmpty()) {
                showAlert("Vui lòng nhập mã cuộn và mã nhân viên để mở lại.");
                return;
            }
            handleReopenReturn(rollCode, employeeId);
        });
    }

    private void setupTable() {
        colNo.setCellValueFactory(cell -> new SimpleIntegerProperty(tblReturnedMaterials.getItems().indexOf(cell.getValue()) + 1));
        colRollCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRollCode()));
        colSapCode.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSapCode()));
        colQuantity.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getActualReturned()));
        colFromWarehouse.setCellValueFactory(cell -> new SimpleStringProperty(getWarehouseName(cell.getValue().getTransferId(), true)));
        colToWarehouse.setCellValueFactory(cell -> new SimpleStringProperty("SMT Warehouse"));
        colNote.setCellValueFactory(cell -> new SimpleStringProperty("Trả về SMT"));

        tblReturnedMaterials.setItems(returnedList);
    }

    private void handleReturnMaterial() {
        String rollCode = txtBarcode.getText().trim();
        String employeeId = txtEmployeeId.getText().trim();
        String qtyStr = txtReturnQty.getText().trim();

        if (rollCode.isEmpty() || employeeId.isEmpty() || qtyStr.isEmpty()) {
            showAlert("Vui lòng nhập đủ thông tin.");
            return;
        }

        int returnQty;
        try {
            returnQty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            showAlert("Số lượng không hợp lệ.");
            return;
        }

        // Tìm detail theo roll code
        Optional<WarehouseTransferDetail> optionalDetail = warehouseTransferService
                .getDetailRepository()
                .findByRollCode(rollCode);

        if (optionalDetail.isEmpty()) {
            showAlert("Không tìm thấy dữ liệu chuyển kho cho cuộn này.");
            return;
        }

        WarehouseTransferDetail detail = optionalDetail.get();

        // ❗❗ Kiểm tra nếu cuộn đã được trả trước đó
        if (!detail.isActive()) {
            showAlert("Cuộn này đã được trả về trước đó. Vui lòng kiểm tra lại.");
            return;
        }

        // ✅ Cập nhật thông tin đã trả vào DB
        warehouseTransferService.getDetailRepository()
                .updateReturnInfo(detail.getRollCode(), returnQty, false);

        // ✅ Cập nhật lại object đang giữ
        detail.setActualReturned(returnQty);
        detail.setActive(false);

        // ✅ Cập nhật kho vật liệu
        Material material = materialService.getMaterialByRollCode(rollCode);
        int fromWarehouseId = material.getWarehouseId(); // kho trước khi cập nhật
        int smtWarehouseId = 16;
        material.setWarehouseId(smtWarehouseId);
        material.setQuantity(returnQty);
        materialService.updateMaterial(material);

        // ✅ Ghi log trả
        TransferLog log = new TransferLog();
        log.setTransferId(detail.getTransferId());
        log.setRollCode(rollCode);
        log.setFromWarehouseId(fromWarehouseId);
        log.setToWarehouseId(smtWarehouseId);
        log.setTransferDate(LocalDateTime.now());
        log.setNote("Trả về SMT");
        log.setEmployeeId(employeeId);
        transferLogService.addTransfer(log);

        // ✅ Thêm vào danh sách hiển thị
        returnedList.add(detail);

        // ✅ Xóa input
        txtBarcode.clear();
        txtReturnQty.clear();
    }

    private void handleReopenReturn(String rollCode, String employeeId) {
        Optional<WarehouseTransferDetail> optionalDetail = warehouseTransferService
                .getDetailRepository()
                .findByRollCode(rollCode);

        if (optionalDetail.isEmpty()) {
            showAlert("Không tìm thấy cuộn để khôi phục.");
            return;
        }

        WarehouseTransferDetail detail = optionalDetail.get();

        // ✅ Mở lại cuộn
        warehouseTransferService.getDetailRepository().reopenReturn(rollCode);

        // ✅ Ghi log "phục hồi cuộn"
        Material material = materialService.getMaterialByRollCode(rollCode);
        int fromWarehouseId = material.getWarehouseId();  // hiện tại là SMT
        int toWarehouseId = getWarehouseIdFromTransfer(detail.getTransferId()); // kho ban đầu (nơi chuyển từ)

        TransferLog log = new TransferLog();
        log.setTransferId(detail.getTransferId());
        log.setRollCode(rollCode);
        log.setFromWarehouseId(fromWarehouseId);
        log.setToWarehouseId(toWarehouseId); // quay lại kho trước
        log.setTransferDate(LocalDateTime.now());
        log.setNote("Mở lại cuộn đã trả");
        log.setEmployeeId(employeeId);

        transferLogService.addTransfer(log);

        // ✅ Cập nhật vật liệu (nếu muốn chuyển về lại kho ban đầu)
        material.setWarehouseId(toWarehouseId);
        materialService.updateMaterial(material);

        showAlert("Đã mở lại cuộn và ghi nhận phục hồi.");
    }


    private String getWarehouseName(int transferId, boolean from) {
        return from
                ? warehouseService.getWarehouseNameByTransferId(transferId, true)
                : warehouseService.getWarehouseNameByTransferId(transferId, false);
    }
    private int getWarehouseIdFromTransfer(int transferId) {
        return warehouseTransferService.getFromWarehouseIdByTransferId(transferId);
    }


    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }
}
