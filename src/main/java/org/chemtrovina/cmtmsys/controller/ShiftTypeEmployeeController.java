package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chemtrovina.cmtmsys.model.ShiftTypeEmployee;
import org.chemtrovina.cmtmsys.service.base.ShiftTypeEmployeeService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // 👈 Đảm bảo bạn đã thêm dependency SLF4J/Logback

@Component
public class ShiftTypeEmployeeController {

    // === FXML Components ===
    @FXML private TextField txtShiftCode;
    @FXML private TextField txtShiftName;
    @FXML private TextArea txtDescription;
    @FXML private Button btnSave;
    @FXML private Button btnNew;
    @FXML private Button btnDelete;
    @FXML private TableView<ShiftTypeEmployee> tblShiftTypes;
    @FXML private TableColumn<ShiftTypeEmployee, String> colShiftCode;
    @FXML private TableColumn<ShiftTypeEmployee, String> colShiftName;
    @FXML private TableColumn<ShiftTypeEmployee, String> colDescription;

    // === Dependencies ===
    private final ShiftTypeEmployeeService shiftTypeEmployeeService;

    // 🔥 ĐÃ THÊM: Khai báo Logger
    private static final Logger logger = LoggerFactory.getLogger(ShiftTypeEmployeeController.class);


    // === State ===
    private ShiftTypeEmployee selectedShiftType = null;

    // Spring Injection
    public ShiftTypeEmployeeController(ShiftTypeEmployeeService shiftTypeEmployeeService) {
        this.shiftTypeEmployeeService = shiftTypeEmployeeService;
    }


    // === Initialization ===
    @FXML
    public void initialize() {
        setupTable();
        loadTableData();
        setupEventHandlers();
        clearForm();
        FxClipboardUtils.enableCopyShortcut(tblShiftTypes);
    }

    private void setupTable() {
        colShiftCode.setCellValueFactory(new PropertyValueFactory<>("shiftCode"));
        colShiftName.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        tblShiftTypes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
                btnDelete.setDisable(false);
            } else {
                clearForm();
            }
        });
    }

    private void setupEventHandlers() {
        btnSave.setOnAction(e -> handleSaveOrUpdate());
        btnNew.setOnAction(e -> clearForm());
        btnDelete.setOnAction(e -> handleDelete());
    }

    // === CRUD Operations ===

    private void handleSaveOrUpdate() {
        if (!validateForm()) return;

        ShiftTypeEmployee type = createTypeFromForm();

        try {
            if (selectedShiftType == null) {
                // THÊM MỚI
                if (shiftTypeEmployeeService.getByCode(type.getShiftCode()) != null) {
                    FxAlertUtils.warning("Mã Ca đã tồn tại. Vui lòng chọn Mã khác hoặc cập nhật.");
                    return;
                }
                shiftTypeEmployeeService.create(type);
                FxAlertUtils.info("Thêm mới thành công!");
            } else {
                // CẬP NHẬT
                shiftTypeEmployeeService.update(type);
                FxAlertUtils.info("Cập nhật thành công!");
            }

            loadTableData();
            clearForm();

        } catch (Exception ex) {
            // 🔥 ĐÃ THÊM: Ghi lỗi chi tiết vào file log (cùng với Stack Trace)
            logger.error("Lỗi khi lưu/cập nhật loại ca {}: {}", type.getShiftCode(), ex.getMessage(), ex);

            // Thông báo lỗi cho người dùng
            FxAlertUtils.error("Lỗi thao tác dữ liệu: Không thể lưu dữ liệu - " + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedShiftType == null) return;

        boolean confirm = FxAlertUtils.confirm(
                "Xác nhận xóa",
                "Bạn có chắc chắn muốn xóa Mã Ca: " + selectedShiftType.getShiftCode() + "?"
        );

        if (confirm) {
            try {
                shiftTypeEmployeeService.delete(selectedShiftType.getShiftCode());
                loadTableData();
                clearForm();
                FxAlertUtils.info("Xóa thành công!");
            } catch (Exception ex) {
                // 🔥 ĐÃ THÊM: Ghi lỗi chi tiết vào file log (cùng với Stack Trace)
                logger.error("Lỗi khi xóa loại ca {}: {}", selectedShiftType.getShiftCode(), ex.getMessage(), ex);

                // Thông báo lỗi cho người dùng
                FxAlertUtils.error("Lỗi xóa dữ liệu: Không thể xóa dữ liệu - " + ex.getMessage());
            }
        }
    }


    // === Data & Form Helpers ===

    private void loadTableData() {
        List<ShiftTypeEmployee> list = shiftTypeEmployeeService.getAll();
        tblShiftTypes.setItems(FXCollections.observableArrayList(list));
    }

    private boolean validateForm() {
        if (txtShiftCode.getText().isBlank()) {
            FxAlertUtils.warning("Mã Ca không được để trống.");
            return false;
        }
        if (txtShiftName.getText().isBlank()) {
            FxAlertUtils.warning("Tên Ca không được để trống.");
            return false;
        }
        return true;
    }

    private ShiftTypeEmployee createTypeFromForm() {
        ShiftTypeEmployee type = new ShiftTypeEmployee();
        type.setShiftCode(txtShiftCode.getText().trim());
        type.setShiftName(txtShiftName.getText().trim());
        type.setDescription(txtDescription.getText().trim());
        return type;
    }

    private void fillForm(ShiftTypeEmployee type) {
        selectedShiftType = type;
        txtShiftCode.setText(type.getShiftCode());
        txtShiftName.setText(type.getShiftName());
        txtDescription.setText(type.getDescription());

        txtShiftCode.setEditable(false);
        btnSave.setText("CẬP NHẬT");
        btnDelete.setDisable(false);
    }

    private void clearForm() {
        selectedShiftType = null;
        txtShiftCode.clear();
        txtShiftName.clear();
        txtDescription.clear();

        txtShiftCode.setEditable(true);
        btnSave.setText("LƯU MỚI");
        btnDelete.setDisable(true);
        tblShiftTypes.getSelectionModel().clearSelection();
    }
}