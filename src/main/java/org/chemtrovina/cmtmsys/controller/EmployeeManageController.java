package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.DepartmentSummaryDto;
import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.time.LocalDate;
import java.util.*;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.EMPLOYEE,
        UserRole.EMPLOYEE_MINI
})

@Component
public class EmployeeManageController {

    @FXML private TableView<EmployeeDto> tblEmployee;
    @FXML private TableColumn<EmployeeDto, Integer> colNo;
    @FXML private TableColumn<EmployeeDto, String> colMSCNID1;
    @FXML private TableColumn<EmployeeDto, String> colMSCNID2;
    @FXML private TableColumn<EmployeeDto, String> colFullName;
    @FXML private TableColumn<EmployeeDto, String> colCompany;
    @FXML private TableColumn<EmployeeDto, String> colDepartment;
    @FXML private TableColumn<EmployeeDto, String> colGender;
    @FXML private TableColumn<EmployeeDto, String> colPosition;
    @FXML private TableColumn<EmployeeDto, String> colManager;
    @FXML private TableColumn<EmployeeDto, String> colDateOfBirth;
    @FXML private TableColumn<EmployeeDto, String> colEntryDate;
    @FXML private TableColumn<EmployeeDto, String> colShift;
    @FXML private TableColumn<EmployeeDto, String> colPhoneNumber;
    @FXML private TableColumn<EmployeeDto, String> colJobTitle;
    @FXML private TableColumn<EmployeeDto, String> colNote;
    @FXML private TableColumn<EmployeeDto, String> colStatus;
    @FXML private TableColumn<EmployeeDto, String> colExitDate;

    @FXML private TableView<DepartmentSummaryDto> tblSummary;
    @FXML private TableColumn<DepartmentSummaryDto, String> colSummaryDepartment;
    @FXML private TableColumn<DepartmentSummaryDto, Integer> colSummaryTotal;
    @FXML private TableColumn<DepartmentSummaryDto, Integer> colSummaryCHEM;
    @FXML private TableColumn<DepartmentSummaryDto, Integer> colSummaryTV;


    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private DatePicker dpEntryDateFrom, dpEntryDateTo;
    @FXML private Button btnFilter, btnClearFilter;
    @FXML private Button btnImportExcel;


    //private final ContextMenu departmentFilterMenu = new ContextMenu();
    private final Map<String, CheckBox> departmentCheckItems = new HashMap<>();
    private final List<String> selectedDepartments = new ArrayList<>();

    private List<String> selectedCompanies = new ArrayList<>();

    private boolean showCompanyColumns = false;
    private final List<TableColumn<DepartmentSummaryDto, ?>> dynamicCompanyColumns = new ArrayList<>();


    private final EmployeeService employeeService;
    @Autowired
    public EmployeeManageController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        loadEmployeeTable();
        setupActions();
        setupSummaryColumns();
        FxClipboardUtils.enableCopyShortcut(tblEmployee);
        FxClipboardUtils.enableCopyShortcut(tblSummary);
        setupContextMenu();
        //tblEmployee.setEditable(true);
    }


    ///////////////////////////////////////SET_UP/////////////////////////////////////


    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colMSCNID1.setCellValueFactory(new PropertyValueFactory<>("mscnId1"));
        colMSCNID2.setCellValueFactory(new PropertyValueFactory<>("mscnId2"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colCompany.setCellValueFactory(new PropertyValueFactory<>("company"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("positionName"));
        colManager.setCellValueFactory(new PropertyValueFactory<>("managerName"));
        colDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colEntryDate.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // nếu có enum -> text
        colExitDate.setCellValueFactory(new PropertyValueFactory<>("exitDate"));


        colManager.setCellFactory(TextFieldTableCell.forTableColumn());

        colManager.setOnEditCommit(event -> {
            EmployeeDto dto = event.getRowValue();
            String newManagerName = event.getNewValue();

            if (newManagerName == null) return;

            newManagerName = newManagerName.trim();
            dto.setManagerName(newManagerName);

            employeeService.updateManager(dto.getEmployeeId(), newManagerName);
            tblEmployee.refresh();
        });



    }

    private void setupSummaryColumns() {
        colSummaryDepartment.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDepartment()));
        colSummaryTotal.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTotal()).asObject());
        colSummaryCHEM.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getChem()).asObject());
        colSummaryTV.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTv()).asObject());

        Hyperlink toggleDetailLink = new Hyperlink("Hiện chi tiết");
        toggleDetailLink.setOnAction(e -> {
            showCompanyColumns = !showCompanyColumns;
            toggleDetailLink.setText(showCompanyColumns ? "Ẩn chi tiết" : "Hiện chi tiết");

            // Reload lại bảng để thêm/bỏ cột động
            loadSummaryTable(tblEmployee.getItems());
        });

        Label label = new Label("TV");
        VBox headerBox = new VBox(label, toggleDetailLink);
        headerBox.setSpacing(2);
        colSummaryTV.setGraphic(headerBox);


    }


    private void setupActions() {
        btnFilter.setOnAction(event -> applyFilter());
        btnClearFilter.setOnAction(event -> clearFilter());

        btnImportExcel.setOnAction(event -> importExcel());

       tblSummary.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblSummary);
            }
        });

        tblEmployee.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                FxClipboardUtils.copySelectionToClipboard(tblEmployee);
            }
        });


    }

    private void setupStatusFilter() {
        cbStatusFilter.getItems().add("Tất cả");
        for (EmployeeStatus status : EmployeeStatus.values()) {
            cbStatusFilter.getItems().add(status.getLabel());
        }
        cbStatusFilter.setValue("Tất cả");

    }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem updateItem = new MenuItem("✏ Update");
        MenuItem deleteItem = new MenuItem("🗑 Delete");

        updateItem.setOnAction(e -> openUpdateDialog());
        deleteItem.setOnAction(e -> deleteSelectedEmployee());

        menu.getItems().addAll(updateItem, deleteItem);

        tblEmployee.setContextMenu(menu);

        /*// Disable menu nếu không chọn dòng
        tblEmployee.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> menu.setDisable(newVal == null)
        );*/
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void applyFilter() {

        String selectedLabel = cbStatusFilter.getValue();
        EmployeeStatus selectedStatus = null;

        if (selectedLabel != null && !"Tất cả".equals(selectedLabel)) {
            selectedStatus = Arrays.stream(EmployeeStatus.values())
                    .filter(s -> s.getLabel().equals(selectedLabel))
                    .findFirst()
                    .orElse(null);
        }

        LocalDate from = dpEntryDateFrom.getValue();
        LocalDate to = dpEntryDateTo.getValue();

        var filtered = employeeService.filterEmployeeDtos(selectedStatus, from, to);
        tblEmployee.setItems(FXCollections.observableArrayList(filtered));
        loadSummaryTable(filtered);
    }


    private void clearFilter() {
        cbStatusFilter.setValue(null);
        dpEntryDateFrom.setValue(null);
        dpEntryDateTo.setValue(null);
        loadEmployeeTable();
    }

    private void filterGeneral(List<String> selected) {

        List<EmployeeDto> all = employeeService.getAllEmployeeDtos();

        List<EmployeeDto> filtered = all.stream()
                .filter(e -> selected.contains(
                                Objects.toString(e.getMscnId1(), "")
                        ) ||
                                selected.contains(Objects.toString(e.getMscnId2(), "")) ||
                                selected.contains(Objects.toString(e.getFullName(), "")) ||
                                selected.contains(Objects.toString(e.getGender(), "")) ||
                                selected.contains(Objects.toString(e.getPositionName(), "")) ||
                                selected.contains(Objects.toString(e.getManagerName(), "")) ||
                                selected.contains(Objects.toString(e.getPhoneNumber(), "")) ||
                                selected.contains(Objects.toString(e.getJobTitle(), "")) ||
                                selected.contains(Objects.toString(e.getStatus(), ""))
                )
                .toList();

        tblEmployee.setItems(FXCollections.observableArrayList(filtered));
        loadSummaryTable(filtered);
    }



    private void applyDepartmentFilter(List<String> selectedDepartments) {
        List<EmployeeDto> all = employeeService.getAllEmployeeDtos();
        List<EmployeeDto> filtered = all.stream()
                .filter(emp -> selectedDepartments.contains(emp.getDepartmentName()))
                .toList();

        tblEmployee.setItems(FXCollections.observableArrayList(filtered));
        loadSummaryTable(filtered); // nếu muốn update bảng tổng hợp
    }

    private void applyCompanyFilter(List<String> selected) {
        selectedCompanies = selected;

        List<EmployeeDto> all = employeeService.getAllEmployeeDtos();
        List<EmployeeDto> filtered = all.stream()
                .filter(e -> selectedCompanies.contains(e.getCompany()))
                .toList();

        tblEmployee.setItems(FXCollections.observableArrayList(filtered));
        loadSummaryTable(filtered);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadSummaryTable(List<EmployeeDto> employeeList) {
        Map<String, DepartmentSummaryDto> summaryMap = new HashMap<>();
        Set<String> dynamicCompanies = new HashSet<>();

        for (EmployeeDto emp : employeeList) {
            String department = emp.getDepartmentName();
            if (department == null || department.isBlank()) {
                department = "UNKNOWN";
            }

            String company = emp.getCompany();
            if (company == null || company.isBlank()) continue;

            boolean isChem = "CHEM".equalsIgnoreCase(company);
            DepartmentSummaryDto summary = summaryMap.computeIfAbsent(department, DepartmentSummaryDto::new);
            summary.add(isChem);

            if (!isChem) {
                summary.addCompany(company);
                dynamicCompanies.add(company);
            }
        }

        var summaries = new ArrayList<>(summaryMap.values());
        tblSummary.setItems(FXCollections.observableArrayList(summaries));

        // Xoá cột công ty động cũ (nếu có)
        tblSummary.getColumns().removeAll(dynamicCompanyColumns);
        dynamicCompanyColumns.clear();

        // Chỉ thêm nếu đang bật chế độ hiển thị
        if (showCompanyColumns) {
            for (String company : dynamicCompanies.stream().sorted().toList()) {
                TableColumn<DepartmentSummaryDto, Integer> col = new TableColumn<>(company);
                col.setCellValueFactory(cell -> {
                    Integer count = cell.getValue().getCompanyCounts().getOrDefault(company, 0);
                    return new SimpleIntegerProperty(count).asObject();
                });
                col.setUserData("dynamic"); // Đánh dấu để dễ xoá
                dynamicCompanyColumns.add(col);
            }

            // Thêm vào bên phải colSummaryTV
            int tvIndex = tblSummary.getColumns().indexOf(colSummaryTV);
            if (tvIndex >= 0) {
                tblSummary.getColumns().addAll(tvIndex + 1, dynamicCompanyColumns);
            } else {
                tblSummary.getColumns().addAll(dynamicCompanyColumns); // fallback
            }
        }
    }


    private void loadEmployeeTable() {
        List<EmployeeDto> employees = employeeService.getAllEmployeeDtos();
        tblEmployee.setItems(FXCollections.observableArrayList(employees));
        loadSummaryTable(employees);
        //setupDepartmentFilterMenu(employees);

        FxFilterUtils.setupFilterMenu(colDepartment, employees, EmployeeDto::getDepartmentName, this::applyDepartmentFilter);
        FxFilterUtils.setupFilterMenu(colCompany, employees, EmployeeDto::getCompany, this::applyCompanyFilter);

        // Các cột còn lại (không cần custom logic)
        FxFilterUtils.setupFilterMenu(colMSCNID1, employees, EmployeeDto::getMscnId1, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colMSCNID2, employees, EmployeeDto::getMscnId2, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colFullName, employees, EmployeeDto::getFullName, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colGender, employees, EmployeeDto::getGender, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colPosition, employees, EmployeeDto::getPositionName, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colManager, employees, EmployeeDto::getManagerName, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colDateOfBirth, employees, e -> String.valueOf(e.getBirthDate()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colEntryDate, employees, e -> String.valueOf(e.getEntryDate()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colExitDate, employees, e -> String.valueOf(e.getExitDate()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colShift, employees, EmployeeDto::getShiftName, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colPhoneNumber, employees, EmployeeDto::getPhoneNumber, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colJobTitle, employees, EmployeeDto::getJobTitle, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colNote, employees, EmployeeDto::getNote, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colStatus, employees, EmployeeDto::getStatus, this::filterGeneral);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void importExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel nhân viên");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File file = fileChooser.showOpenDialog(tblEmployee.getScene().getWindow());
        if (file == null) return;

        DatePicker dp = new DatePicker(LocalDate.now());
        Alert dateDialog = new Alert(Alert.AlertType.CONFIRMATION);
        dateDialog.setTitle("Chọn ngày import");
        dateDialog.setHeaderText("Chọn ngày import (ngày sẽ cắt tên nếu thiếu trong file)");
        dateDialog.getDialogPane().setContent(dp);

        if (dateDialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        LocalDate importDate = dp.getValue();
        if (importDate == null) {
            FxAlertUtils.warning("Vui lòng chọn ngày import.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận import");
        confirm.setHeaderText("Import nhân viên từ Excel - ngày: " + importDate);
        confirm.setContentText("""
Dữ liệu trùng MSCNID1 sẽ được cập nhật.
Nhân viên ACTIVE không có trong file sẽ bị CẮT TÊN (INACTIVE) với ExitDate = ngày import.
Bạn có chắc muốn tiếp tục?
""");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            employeeService.importEmployeeFromExcel(file, importDate);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText(null);
            success.setContentText("Import Excel thành công!");
            success.showAndWait();

            loadEmployeeTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Lỗi import");
            error.setHeaderText("Import Excel thất bại");
            error.setContentText(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
            error.showAndWait();
        }
    }


    private void deleteSelectedEmployee() {
        EmployeeDto dto = tblEmployee.getSelectionModel().getSelectedItem();
        if (dto == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText("Xoá nhân viên");
        confirm.setContentText("Bạn có chắc muốn xoá nhân viên này?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        employeeService.deleteEmployeeById(dto.getEmployeeId());
        loadEmployeeTable();
    }

    private void openUpdateDialog() {
        EmployeeDto dto = tblEmployee.getSelectionModel().getSelectedItem();
        if (dto == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/chemtrovina/cmtmsys/view/EmployeeUpdateDialog.fxml")
            );
            Parent root = loader.load();

            EmployeeUpdateDialogController controller = loader.getController();
            controller.setEmployee(dto);

            Stage stage = new Stage();
            stage.setTitle("Cập nhật nhân viên");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadEmployeeTable();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
