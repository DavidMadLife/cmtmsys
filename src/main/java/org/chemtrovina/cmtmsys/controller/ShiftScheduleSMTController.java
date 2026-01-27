package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.chemtrovina.cmtmsys.model.ShiftScheduleSMT;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.ShiftScheduleSMTService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.TableColumnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class ShiftScheduleSMTController {

    @FXML private ComboBox<Warehouse> cbWarehouse;
    @FXML private DatePicker dpShiftDate;
    @FXML private ComboBox<String> cbShiftType;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private Button btnAddShift;

    @FXML private TableView<ShiftScheduleSMT> tblShifts;
    @FXML private TableColumn<ShiftScheduleSMT, Integer> colShiftId;
    @FXML private TableColumn<ShiftScheduleSMT, String> colWarehouse;
    @FXML private TableColumn<ShiftScheduleSMT, String> colShiftDate;
    @FXML private TableColumn<ShiftScheduleSMT, String> colShiftType;
    @FXML private TableColumn<ShiftScheduleSMT, String> colStartTime;
    @FXML private TableColumn<ShiftScheduleSMT, String> colEndTime;
    @FXML private TableColumn<ShiftScheduleSMT, String> colCreatedAt;

    private final ShiftScheduleSMTService shiftService;
    private final WarehouseService warehouseService;

    @Autowired
    public ShiftScheduleSMTController(ShiftScheduleSMTService shiftService, WarehouseService warehouseService) {
        this.shiftService = shiftService;
        this.warehouseService = warehouseService;
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableColumns();
        setupEditableColumns();

        loadShifts();
        btnAddShift.setOnAction(e -> onAddShift());
        FxClipboardUtils.enableCopyShortcut(tblShifts );
    }

    // ========================= SETUP =========================

    private void setupComboBoxes() {
        cbShiftType.setItems(FXCollections.observableArrayList("DAY", "NIGHT"));
        cbWarehouse.setItems(FXCollections.observableArrayList(warehouseService.getAllWarehouses()));

        cbWarehouse.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbWarehouse.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void setupTableColumns() {
        TableColumnUtils.setIntegerColumn(colShiftId, ShiftScheduleSMT::getShiftId);
        TableColumnUtils.setStringColumn(colWarehouse,
                s -> warehouseService.getWarehouseNameById(s.getWarehouseId()));
        TableColumnUtils.setStringColumn(colShiftDate,
                s -> s.getShiftDate().toLocalDate().toString());
        TableColumnUtils.setStringColumn(colShiftType, ShiftScheduleSMT::getShiftType);
        TableColumnUtils.setStringColumn(colStartTime,
                s -> s.getStartTime().toLocalTime().toString());
        TableColumnUtils.setStringColumn(colEndTime,
                s -> s.getEndTime().toLocalTime().toString());
        TableColumnUtils.setStringColumn(colCreatedAt,
                s -> s.getCreatedAt() != null ? s.getCreatedAt().toString() : "");
    }

    private void setupEditableColumns() {
        tblShifts.setEditable(true);

        // Ca
        colShiftType.setCellFactory(TextFieldTableCell.forTableColumn());
        colShiftType.setOnEditCommit(event -> {
            ShiftScheduleSMT shift = event.getRowValue();
            shift.setShiftType(event.getNewValue());
            updateShift(shift, "✅ Đã cập nhật loại ca.");
        });

        // Giờ bắt đầu
        colStartTime.setCellFactory(TextFieldTableCell.forTableColumn());
        colStartTime.setOnEditCommit(event -> {
            ShiftScheduleSMT shift = event.getRowValue();
            try {
                LocalTime newStart = LocalTime.parse(event.getNewValue());
                shift.setStartTime(LocalDateTime.of(shift.getShiftDate().toLocalDate(), newStart));
                updateShift(shift, "✅ Đã cập nhật giờ bắt đầu.");
            } catch (Exception e) {
                FxAlertUtils.error("❌ Sai định dạng giờ (HH:mm).");
                loadShifts();
            }
        });

        // Giờ kết thúc
        colEndTime.setCellFactory(TextFieldTableCell.forTableColumn());
        colEndTime.setOnEditCommit(event -> {
            ShiftScheduleSMT shift = event.getRowValue();
            try {
                LocalTime newEnd = LocalTime.parse(event.getNewValue());
                LocalDate date = shift.getShiftDate().toLocalDate();
                if ("NIGHT".equalsIgnoreCase(shift.getShiftType()) &&
                        newEnd.isBefore(shift.getStartTime().toLocalTime())) {
                    shift.setEndTime(LocalDateTime.of(date.plusDays(1), newEnd));
                } else {
                    shift.setEndTime(LocalDateTime.of(date, newEnd));
                }
                updateShift(shift, "✅ Đã cập nhật giờ kết thúc.");
            } catch (Exception e) {
                FxAlertUtils.error("❌ Sai định dạng giờ (HH:mm).");
                loadShifts();
            }
        });
    }

    // ========================= CRUD =========================

    private void loadShifts() {
        tblShifts.setItems(FXCollections.observableArrayList(shiftService.getAllShifts()));
    }

    private void onAddShift() {
        Warehouse wh = cbWarehouse.getValue();
        LocalDate date = dpShiftDate.getValue();
        String type = cbShiftType.getValue();

        if (wh == null || date == null || type == null) {
            FxAlertUtils.warning("⚠️ Vui lòng nhập đủ thông tin ca.");
            return;
        }

        try {
            LocalTime start = LocalTime.parse(txtStartTime.getText().trim());
            LocalTime end = LocalTime.parse(txtEndTime.getText().trim());

            ShiftScheduleSMT shift = new ShiftScheduleSMT();
            shift.setWarehouseId(wh.getWarehouseId());
            shift.setShiftDate(date.atStartOfDay());
            shift.setShiftType(type);
            shift.setStartTime(LocalDateTime.of(date, start));
            shift.setEndTime(LocalDateTime.of(type.equals("DAY") ? date : date.plusDays(1), end));
            shiftService.addShift(shift);

            FxAlertUtils.info("✅ Đã tạo ca mới.");
            loadShifts();
        } catch (Exception ex) {
            FxAlertUtils.error("❌ Lỗi: " + ex.getMessage());
        }
    }

    private void updateShift(ShiftScheduleSMT shift, String successMsg) {
        try {
            shiftService.updateShift(shift);
            FxAlertUtils.info(successMsg);
            loadShifts();
        } catch (Exception ex) {
            FxAlertUtils.error("❌ Lỗi khi cập nhật: " + ex.getMessage());
            loadShifts();
        }
    }
}
