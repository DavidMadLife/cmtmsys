package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import org.chemtrovina.cmtmsys.dto.SolderSessionUpdate;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.Solder;
import org.chemtrovina.cmtmsys.model.SolderSession;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.chemtrovina.cmtmsys.service.base.SolderService;
import org.chemtrovina.cmtmsys.service.base.SolderSessionService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class SolderOutController {

    // ====== Top bar ======
    @FXML private TextField tfCode;
    @FXML private TextField tfSearch;
    @FXML private Button btnSearch, btnClearSearch;
    @FXML private Spinner<Integer> spAgingMin;
    @FXML private DatePicker dpOutDate;
    @FXML private Button btnScan, btnReload;


    // ====== Table & columns ======
    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row, Number> colSTT;
    @FXML private TableColumn<Row, String> colCode, colMaker, colLot, colNote;
    @FXML private TableColumn<Row, Double> colViscotester;
    @FXML private TableColumn<Row, LocalDate> colOut;
    @FXML private TableColumn<Row, LocalDateTime> colStart, colEnd;

    // Receive columns
    @FXML private TableColumn<Row, String> colWarehouse;
    @FXML private TableColumn<Row, String> colEmpRecvId;
    @FXML private TableColumn<Row, String> colEmpRecvName;
    @FXML private TableColumn<Row, LocalDateTime> colOpenTime;

    // Return / scrap
    @FXML private TableColumn<Row, LocalDateTime> colReturnTime, colScrapTime;
    @FXML private TableColumn<Row, String> colEmpRetId, colEmpRetName, colStatus;

    @FXML private Label lblTotal;

    // ====== Services ======
    private final SolderSessionService sessionService;
    private final SolderService solderService;
    private final WarehouseService warehouseService;
    private final EmployeeService employeeService;

    // ====== Caches & options ======
    private final Map<Integer, Solder> solderCache = new HashMap<>();
    private final Map<Integer, Employee> empCache = new HashMap<>();
    private final Map<Integer, String> whNameCache = new HashMap<>();
    private final Map<String, Integer> whIdByName = new HashMap<>();
    private List<String> warehouseNames = new ArrayList<>();

    // ====== Formats ======
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ====== Data nguồn cho Table (lọc/sắp xếp) ======
    private final ObservableList<Row> master = FXCollections.observableArrayList();
    private FilteredList<Row> filtered;

    @Autowired
    public SolderOutController(SolderSessionService sessionService,
                               SolderService solderService,
                               WarehouseService warehouseService,
                               EmployeeService employeeService) {
        this.sessionService = sessionService;
        this.solderService = solderService;
        this.warehouseService = warehouseService;
        this.employeeService = employeeService;
    }

    // ============================== INIT ==============================

    @FXML
    public void initialize() {
        initDefaults();
        loadWarehouseOptions();
        configureTable();
        configureActions();
        reloadForDate(dpOutDate.getValue());

    }

    private void initDefaults() {
        // Để trống => load tất cả
        dpOutDate.setValue(null);
        dpOutDate.getEditor().setPromptText("Tất cả ngày");
        spAgingMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 600, 120, 10));
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }


    private void loadWarehouseOptions() {
        warehouseNames.clear(); whIdByName.clear();
        for (Warehouse w : warehouseService.getAllWarehouses()) {
            warehouseNames.add(w.getName());
            whIdByName.put(w.getName(), w.getWarehouseId());
            whNameCache.put(w.getWarehouseId(), w.getName());
        }
        warehouseNames.sort(String::compareToIgnoreCase);
    }

    private void configureTable() {
        configureCommonColumns();
        configureReceiveColumns();
        configureReturnScrapColumns();
        configureExcelUX();
        configureRowContextMenu();
    }

    private void configureCommonColumns() {
        colSTT.setCellValueFactory(cd ->
                Bindings.createIntegerBinding(() -> tbl.getItems().indexOf(cd.getValue()) + 1));

        colCode.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().code));
        colMaker.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().maker));
        colLot.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().lot));
        colViscotester.setCellValueFactory(cd ->new SimpleObjectProperty<>(cd.getValue().viscotester));
        colNote.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().note));

        colOut.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().outDate));
        colOut.setCellFactory(c -> dateCell());

        colStart.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().agingStart));
        colStart.setCellFactory(c -> timeCell());

        colEnd.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().agingEnd));
        colEnd.setCellFactory(c -> timeCell());
    }

    private void configureReceiveColumns() {
        // Warehouse combobox
        colWarehouse.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().warehouseName));
        colWarehouse.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(warehouseNames)));
        colWarehouse.setOnEditCommit(ev -> onEditWarehouse(ev.getRowValue(), ev.getNewValue()));

        // Emp receive: MSCNID1 editable
        colEmpRecvId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().empRecvScan));
        colEmpRecvId.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmpRecvId.setOnEditCommit(ev -> onEditEmpReceive(ev.getRowValue(), safe(ev.getNewValue())));

        // Emp name (readonly)
        colEmpRecvName.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().receiverEmployeeName));

        // Open time view
        colOpenTime.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().openTime));
        colOpenTime.setCellFactory(c -> dateTimeCell());
    }

    private void configureReturnScrapColumns() {
        // hiển thị thời gian
        colReturnTime.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().returnTime));
        colReturnTime.setCellFactory(c -> dateTimeCell());

        colScrapTime.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().scrapTime));
        colScrapTime.setCellFactory(c -> dateTimeCell());

        // trạng thái (OK/NG/SCRAP...)
        colStatus.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().status));

        // ----- NHỮNG DÒNG BỔ SUNG CHO “TRẢ” -----
        // Mã NV trả: cho phép nhập (MSCNID1)
        colEmpRetId.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().empRetScan));
        colEmpRetId.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmpRetId.setOnEditCommit(ev -> onEditEmpReturn(ev.getRowValue(), safe(ev.getNewValue())));

        // Tên NV trả: chỉ hiển thị
        colEmpRetName.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().returnEmployeeName));
    }

    private void configureExcelUX() {
        tbl.getSelectionModel().setCellSelectionEnabled(true);
        tbl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbl.setEditable(true);
        tbl.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tbl);
            }
        });
    }

    private void configureRowContextMenu() {
        tbl.setRowFactory(tv -> {
            TableRow<Row> row = new TableRow<>();
            MenuItem miDelete = new MenuItem("🗑 Xoá dòng…");
            miDelete.setOnAction(e -> onDeleteRow(row.getItem()));
            ContextMenu menu = new ContextMenu(miDelete);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu)
            );
            row.setOnMousePressed(ev -> {
                if (ev.isSecondaryButtonDown() && !row.isEmpty()) {
                    tbl.getSelectionModel().select(row.getIndex());
                }
            });
            return row;
        });
    }

    private void configureActions() {
        btnScan.setOnAction(e -> doScanOut());
        tfCode.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) doScanOut(); });

        // Nếu muốn luôn “load tất cả” khi bấm Reload, thay dòng dưới bằng: reloadForDate(null);
        btnReload.setOnAction(e -> reloadForDate(dpOutDate.getValue()));

        dpOutDate.valueProperty().addListener((o, ov, nv) -> reloadForDate(nv));

        btnSearch.setOnAction(e -> doSearchCode());
        btnClearSearch.setOnAction( e -> {
            tfSearch.clear();
            reloadForDate(dpOutDate.getValue());
        });

        //tfSearch.setOnKeyPressed(e -> doSearchCode());
    }


    // ============================== HANDLERS ==============================

    private void doScanOut() {
        String code = safe(tfCode.getText());
        if (code.isBlank()) { warn("Quét/nhập code trước."); return; }
        int min = spAgingMin.getValue() == null ? 120 : spAgingMin.getValue();

        try {
            sessionService.scanOut(code, min, null); // tạo session: start=now, end=now+min, outDate=hôm nay
            tfCode.clear();
            reloadForDate(dpOutDate.getValue());     // chỉ reload, KHÔNG set scrapTime ở đây
        } catch (Exception ex) {
            error("Scan OUT lỗi: " + ex.getMessage());
        }
    }



    private void onEditWarehouse(Row r, String newName) {
        r.warehouseName = newName;
        r.warehouseId = newName == null ? null : whIdByName.get(newName);
        try {
            sessionService.updateSession(
                    r.sessionId,
                    new SolderSessionUpdate().withWarehouseId(r.warehouseId)
            );
        } catch (Exception ex) {
            error("Lưu kho/line lỗi: " + ex.getMessage());
            tbl.refresh();
        }
    }


    private void onEditEmpReceive(Row r, String mscnId1) {
        if (r.warehouseId == null) { warn("Chọn Warehouse/Line trước."); tbl.refresh(); return; }
        if (mscnId1.isBlank())     { warn("Mã nhân viên trống.");        tbl.refresh(); return; }

        try {
            Employee emp = employeeService.getByMscnId1(mscnId1);
            if (emp == null) { warn("Không tìm thấy nhân viên: " + mscnId1); tbl.refresh(); return; }

            r.empRecvScan = mscnId1;
            r.receiverEmployeeId = emp.getEmployeeId();
            r.receiverEmployeeName = emp.getFullName();

            // mở nắp tại thời điểm này
            r.openTime = LocalDateTime.now();

            // CHÍNH SÁCH GIỜ PHẾ: theo maker, tính từ thời điểm mở nắp
            int hours = scrapHoursByMaker(r.maker);
            r.scrapTime = r.openTime.plusHours(hours);

            sessionService.updateSession(
                    r.sessionId,
                    new SolderSessionUpdate()
                            .withWarehouseId(r.warehouseId)
                            .withReceiverEmployeeId(r.receiverEmployeeId)
                            .withOpenTime(r.openTime)
                            .withScrapTime(r.scrapTime)
            );
            tbl.refresh();
        } catch (Exception ex) {
            error("Cập nhật nhận hàng lỗi: " + ex.getMessage());
        }
    }


    private void onEditEmpReturn(Row r, String mscnId1) {
        if (r == null) return;

        if (r.openTime == null) {
            warn("Hũ này chưa được nhận/mở nắp – không thể trả.");
            tbl.refresh();
            return;
        }
        if (r.returnTime != null) {
            warn("Hũ đã được trả trước đó.");
            tbl.refresh();
            return;
        }

        if (mscnId1.isBlank()) {
            warn("Mã nhân viên trống.");
            tbl.refresh();
            return;
        }

        try {
            Employee emp = employeeService.getByMscnId1(mscnId1);
            if (emp == null) {
                warn("Không tìm thấy nhân viên với MSCNID1: " + mscnId1);
                tbl.refresh();
                return;
            }

            // Cập nhật dữ liệu dòng hiển thị
            r.empRetScan = mscnId1;
            r.returnEmployeeId = emp.getEmployeeId();
            r.returnEmployeeName = emp.getFullName();
            r.returnTime = LocalDateTime.now();   // nếu muốn UTC: LocalDateTime.now(ZoneOffset.UTC)
            r.status = "OK";

            // Gọi 1 lệnh update duy nhất
            sessionService.updateSession(
                    r.sessionId,
                    new SolderSessionUpdate()
                            .withReturnEmployeeId(r.returnEmployeeId)
                            .withReturnTime(r.returnTime)
                            .withReturnStatus("OK")
            );

            tbl.refresh();
        } catch (Exception ex) {
            error("Cập nhật trả hàng lỗi: " + ex.getMessage());
        }
    }


    private void onDeleteRow(Row r) {
        if (r == null) return;
        Alert cf = new Alert(Alert.AlertType.CONFIRMATION,
                "Xoá phiên OUT của code: " + r.code + " ?", ButtonType.OK, ButtonType.CANCEL);
        cf.setHeaderText("Xoá phiên OUT");
        cf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    sessionService.deleteSession(r.sessionId);
                    reloadForDate(dpOutDate.getValue());
                } catch (Exception ex) {
                    error("Xoá lỗi: " + ex.getMessage());
                }
            }
        });
    }

    // ============================== DATA LOAD ==============================

    private void reloadForDate(LocalDate day) {
        solderCache.clear();
        empCache.clear();

        var sessions = sessionService.getAll(); // lấy tất cả
        if (day != null) {
            sessions = sessions.stream()
                    .filter(s -> Objects.equals(s.getOutDate(), day))
                    .toList();
        }

        List<Row> rows = sessions.stream()
                .map(this::toRow)
                // (tuỳ chọn) sắp xếp cho dễ nhìn: mới nhất trước
                .sorted(Comparator.comparing(
                        (Row r) -> Optional.ofNullable(r.agingStart).orElse(LocalDateTime.MIN)
                ).reversed())
                .toList();

        tbl.setItems(FXCollections.observableArrayList(rows));
        lblTotal.setText(String.valueOf(rows.size()));
    }


    private Row toRow(SolderSession s) {
        Row r = new Row();

        // --- solder info
        Solder so = solderCache.computeIfAbsent(s.getSolderId(), solderService::getSolderById);
        r.sessionId = s.getSessionId();
        r.code  = (so != null) ? so.getCode()  : null;
        r.maker = (so != null) ? so.getMaker() : null;
        r.lot   = (so != null) ? so.getLot()   : null;
        r.viscotester = (so != null) ? so.getViscotester() : null;
        r.note  = s.getNote();

        // --- OUT/Aging
        r.outDate    = s.getOutDate();
        r.agingStart = s.getAgingStartTime();
        r.agingEnd   = s.getAgingEndTime();

        // --- Warehouse (nhận)
        r.warehouseId   = s.getWarehouseId();
        r.warehouseName = (r.warehouseId == null) ? null
                : whNameCache.computeIfAbsent(r.warehouseId, id -> {
            Warehouse w = warehouseService.getWarehouseById(id);
            return (w != null) ? w.getName() : null;
        });

        // --- Receiver (nhận)
        r.receiverEmployeeId = s.getReceiverEmployeeId();
        if (r.receiverEmployeeId != null) {
            Employee empRecv = empCache.computeIfAbsent(r.receiverEmployeeId, employeeService::getEmployeeById);
            if (empRecv != null) {
                r.empRecvScan = safe(empRecv.getMSCNID1());
                r.receiverEmployeeName = empRecv.getFullName();
            }
        }
        r.openTime = s.getOpenTime();

        // --- Return (trả)
        r.returnEmployeeId = s.getReturnEmployeeId();
        if (r.returnEmployeeId != null) {
            Employee empRet = empCache.computeIfAbsent(r.returnEmployeeId, employeeService::getEmployeeById);
            if (empRet != null) {
                r.empRetScan = safe(empRet.getMSCNID1());
                r.returnEmployeeName = empRet.getFullName();
            }
        }
        r.returnTime = s.getReturnTime();

        // --- Scrap/Status
        r.status = s.getReturnStatus();
        // CHỈ hiển thị thời gian phế sau khi đã mở nắp
        r.scrapTime = (r.openTime == null) ? null : s.getScrapTime();

        return r;
    }

    private void doSearchCode() {
        String kw = safe(tfSearch.getText());
        if (kw.isBlank()) { warn("Nhập code cần tìm."); return; }

        try{
            var solders = solderService.searchByCode(kw, 50);
            if(solders.isEmpty()) { warn("Not found code: " + kw); return; }

            var rows = solders.stream()
                    .flatMap(s -> sessionService.getBySolderCode(s.getCode()).stream())
                    .map(this::toRow)
                    .toList();
            tbl.setItems(FXCollections.observableArrayList(rows));
            lblTotal.setText(String.valueOf(rows.size()));
        }
        catch(Exception e){
            error("Search code error: " + e.getMessage());
        }
    }




    // ============================== CELL FACTORIES ==============================

    private TableCell<Row, LocalDate> dateCell() {
        return new TableCell<>() {
            @Override protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATE_FMT.format(item));
            }
        };
    }

    private TableCell<Row, LocalDateTime> timeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : TIME_FMT.format(item));
            }
        };
    }

    private TableCell<Row, LocalDateTime> dateTimeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : DATETIME_FMT.format(item));
            }
        };
    }

    // ============================== Utils & VM ==============================

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private void warn(String m) { new Alert(Alert.AlertType.WARNING, m).showAndWait(); }
    private void error(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
    private int scrapHoursByMaker(String maker) {
        if (maker == null) return 24;
        switch (maker.trim().toUpperCase(Locale.ROOT)) {
            case "HEESUNG":
            case "DUKSAN":
                return 24;
            case "TAMURA":
                return 12;
            default:
                return 24;
        }
    }

    /** View-model cho từng hàng bảng */
    public static class Row {
        int sessionId;
        String code, maker, lot, note;

        LocalDate outDate;
        LocalDateTime agingStart, agingEnd;
        Double viscotester;
        // receive
        Integer warehouseId;
        String warehouseName;
        String empRecvScan;               // MSCN người nhận
        Integer receiverEmployeeId;
        String receiverEmployeeName;
        LocalDateTime openTime;

        // return
        String empRetScan;                // MSCN người trả  ← dùng để nhập
        Integer returnEmployeeId;
        String returnEmployeeName;
        LocalDateTime returnTime;

        // scrap & status
        LocalDateTime scrapTime;
        String status;
    }

}
