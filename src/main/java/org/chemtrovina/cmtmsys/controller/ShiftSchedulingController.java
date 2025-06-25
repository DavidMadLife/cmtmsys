/*
package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.dto.ShiftScheduleDto;
import org.chemtrovina.cmtmsys.model.ShiftSchedule;
import org.chemtrovina.cmtmsys.repository.Impl.EmployeeRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.Impl.ShiftScheduleRepositoryImpl;
import org.chemtrovina.cmtmsys.service.Impl.EmployeeServiceImpl;
import org.chemtrovina.cmtmsys.service.Impl.ShiftScheduleServiceImpl;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.service.base.ShiftScheduleService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@FXML
private TableView<ShiftScheduleDto> tblEmployeeInfo;
@FXML private TableView<ShiftScheduleDto> tblShiftByDate;

@FXML private TableColumn<ShiftScheduleDto, Integer> colNo;
@FXML private TableColumn<ShiftScheduleDto, String> colEmpId;
@FXML private TableColumn<ShiftScheduleDto, String> colFullName;

private final LocalDate fromDate = LocalDate.of(2025, 5, 26);
private final LocalDate toDate = LocalDate.of(2025, 6, 25);
private final List<LocalDate> dateRange = fromDate.datesUntil(toDate.plusDays(1)).toList();

private ShiftScheduleService shiftScheduleService;
private EmployeeService employeeService;

@FXML
public void initialize() {
    setupServices();
    setupFixedColumns();
    loadShiftTable();
}

private void setupServices() {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());
    this.shiftScheduleService = new ShiftScheduleServiceImpl(new ShiftScheduleRepositoryImpl(jdbcTemplate));
    this.employeeService = new EmployeeServiceImpl(new EmployeeRepositoryImpl(jdbcTemplate));
}

private void setupFixedColumns() {
    colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
    colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
    colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
}

private void loadShiftTable() {
    List<EmployeeDto> employees = employeeService.getAllEmployeeDtos();
    List<ShiftSchedule> schedules = shiftScheduleService.getAll();

    Map<Integer, ShiftScheduleDto> dtoMap = new LinkedHashMap<>();
    int index = 1;
    for (EmployeeDto emp : employees) {
        ShiftScheduleDto dto = new ShiftScheduleDto(index++, emp.getMscnId1(), emp.getFullName());
        dtoMap.put(emp.getMscnId1(), dto);
    }

    for (ShiftSchedule schedule : schedules) {
        if (schedule.getWorkDate().isBefore(fromDate) || schedule.getWorkDate().isAfter(toDate)) continue;
        ShiftScheduleDto dto = dtoMap.get(schedule.getEmployeeId());
        if (dto != null) {
            String label = (schedule.getNote() != null && !schedule.getNote().isBlank())
                    ? schedule.getNote()
                    : String.valueOf(schedule.getShiftId()); // hoặc dùng ShiftChemService để lấy tên
            dto.setShift(schedule.getWorkDate(), label);
        }
    }

    ObservableList<ShiftScheduleDto> data = FXCollections.observableArrayList(dtoMap.values());
    tblEmployeeInfo.setItems(data);
    tblShiftByDate.setItems(data);

    createDynamicDateColumns();
}

private void createDynamicDateColumns() {
    tblShiftByDate.getColumns().clear();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

    for (LocalDate date : dateRange) {
        TableColumn<ShiftScheduleDto, String> col = new TableColumn<>(formatter.format(date));
        col.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getShiftForDate(date)));
        col.setPrefWidth(50);
        tblShiftByDate.getColumns().add(col);
    }
}

*/
