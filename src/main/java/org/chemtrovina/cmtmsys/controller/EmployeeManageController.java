package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.DepartmentSummaryDto;
import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.enums.EmployeeStatus;
import org.chemtrovina.cmtmsys.repository.Impl.EmployeeRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.MOQRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.EmployeeRepository;
import org.chemtrovina.cmtmsys.repository.base.MOQRepository;
import org.chemtrovina.cmtmsys.service.Impl.EmployeeServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.MOQServiceImpl;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.utils.FxFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.*;

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
        colDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colEntryDate.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        colShift.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // nếu có enum -> text
        colExitDate.setCellValueFactory(new PropertyValueFactory<>("exitDate"));


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
        tblEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSummary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


    }

    private void setupStatusFilter() {
        cbStatusFilter.getItems().add(null); // default option
        for (EmployeeStatus status : EmployeeStatus.values()) {
            cbStatusFilter.getItems().add(status.getLabel());
        }
    }

    /*private void setupDepartmentFilterMenu(List<EmployeeDto> employees) {
        departmentFilterMenu.getItems().clear();
        departmentCheckItems.clear();
        selectedDepartments.clear();

        Button btnSelectAll = new Button("✓ Chọn tất cả");
        btnSelectAll.setOnAction(e -> departmentCheckItems.values().forEach(cb -> cb.setSelected(true)));
        CustomMenuItem selectAllItem = new CustomMenuItem(btnSelectAll);
        selectAllItem.setHideOnClick(false);

        Button btnDeselectAll = new Button("✗ Bỏ chọn tất cả");
        btnDeselectAll.setOnAction(e -> departmentCheckItems.values().forEach(cb -> cb.setSelected(false)));
        CustomMenuItem deselectAllItem = new CustomMenuItem(btnDeselectAll);
        deselectAllItem.setHideOnClick(false);


        departmentFilterMenu.getItems().addAll(selectAllItem, deselectAllItem, new SeparatorMenuItem());

        // CheckBox theo bộ phận
        employees.stream()
                .map(EmployeeDto::getDepartmentName)
                .filter(dep -> dep != null && !dep.isBlank())
                .distinct()
                .sorted()
                .forEach(dep -> {
                    CheckBox checkBox = new CheckBox(dep);
                    checkBox.setSelected(true);
                    CustomMenuItem item = new CustomMenuItem(checkBox);
                    item.setHideOnClick(false);
                    departmentFilterMenu.getItems().add(item);
                    departmentCheckItems.put(dep, checkBox);
                });

        departmentFilterMenu.getItems().add(new SeparatorMenuItem());

        MenuItem applyFilterItem = new MenuItem("✓ Áp dụng lọc");
        applyFilterItem.setStyle("-fx-font-weight: bold;");
        applyFilterItem.setOnAction(e -> {
            applyDepartmentFilter();
            departmentFilterMenu.hide();
        });

        departmentFilterMenu.getItems().add(applyFilterItem);
        colDepartment.setContextMenu(departmentFilterMenu);
    }*/


    /*private void setupSummaryFilterMenu(List<DepartmentSummaryDto> summaryList) {
        summaryFilterMenu.getItems().clear();
        summaryCheckItems.clear();

        Button btnSelectAll = new Button("✓ Chọn tất cả");
        btnSelectAll.setOnAction(e -> departmentCheckItems.values().forEach(cb -> cb.setSelected(true)));
        CustomMenuItem selectAllItem = new CustomMenuItem(btnSelectAll);
        selectAllItem.setHideOnClick(false);

        Button btnDeselectAll = new Button("✗ Bỏ chọn tất cả");
        btnDeselectAll.setOnAction(e -> departmentCheckItems.values().forEach(cb -> cb.setSelected(false)));
        CustomMenuItem deselectAllItem = new CustomMenuItem(btnDeselectAll);
        deselectAllItem.setHideOnClick(false);


        summaryFilterMenu.getItems().addAll(selectAllItem, deselectAllItem, new SeparatorMenuItem());

        summaryList.stream()
                .map(DepartmentSummaryDto::getDepartment)
                .distinct()
                .sorted()
                .forEach(dep -> {
                    CheckBox checkBox = new CheckBox(dep);
                    checkBox.setSelected(true);
                    CustomMenuItem item = new CustomMenuItem(checkBox);
                    item.setHideOnClick(false);
                    summaryFilterMenu.getItems().add(item);
                    summaryCheckItems.put(dep, checkBox);
                });

        summaryFilterMenu.getItems().add(new SeparatorMenuItem());

        MenuItem apply = new MenuItem("✓ Áp dụng lọc");
        apply.setStyle("-fx-font-weight: bold;");
        apply.setOnAction(e -> {
            filterEmployeeBySummarySelection();
            summaryFilterMenu.hide();
        });

        summaryFilterMenu.getItems().add(apply);
        colSummaryDepartment.setContextMenu(summaryFilterMenu);
    }*/


    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void applyFilter() {
        String selectedLabel = cbStatusFilter.getValue();
        EmployeeStatus selectedStatus = null;

        if (selectedLabel != null) {
            for (EmployeeStatus s : EmployeeStatus.values()) {
                if (s.getLabel().equals(selectedLabel)) {
                    selectedStatus = s;
                    break;
                }
            }
        }

        LocalDate from = dpEntryDateFrom.getValue();
        LocalDate to = dpEntryDateTo.getValue();

        var filtered = employeeService.filterEmployeeDtos(selectedStatus, from, to);
        tblEmployee.setItems(FXCollections.observableArrayList(filtered));
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
                .filter(e ->
                        selected.contains(e.getMscnId1()) ||
                                selected.contains(e.getMscnId2()) ||
                                selected.contains(e.getFullName()) ||
                                selected.contains(e.getGender()) ||
                                selected.contains(e.getPositionName()) ||
                                selected.contains(e.getManagerName()) ||
                                selected.contains(e.getDateOfBirth()) ||
                                selected.contains(e.getEntryDate()) ||
                                selected.contains(e.getExitDate()) ||
                                selected.contains(e.getShiftName()) ||
                                selected.contains(e.getPhoneNumber()) ||
                                selected.contains(e.getJobTitle()) ||
                                selected.contains(e.getNote()) ||
                                selected.contains(e.getStatus())
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
        FxFilterUtils.setupFilterMenu(colDateOfBirth, employees, e -> String.valueOf(e.getDateOfBirth()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colEntryDate, employees, e -> String.valueOf(e.getEntryDate()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colExitDate, employees, e -> String.valueOf(e.getExitDate()), this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colShift, employees, EmployeeDto::getShiftName, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colPhoneNumber, employees, EmployeeDto::getPhoneNumber, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colJobTitle, employees, EmployeeDto::getJobTitle, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colNote, employees, EmployeeDto::getNote, this::filterGeneral);
        FxFilterUtils.setupFilterMenu(colStatus, employees, EmployeeDto::getStatus, this::filterGeneral);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////



}
