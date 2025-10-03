package org.chemtrovina.cmtmsys.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.model.Solder;
import org.chemtrovina.cmtmsys.service.base.SolderService;
import org.chemtrovina.cmtmsys.utils.FxClipboardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class SolderManagerController {

    // ====== FXML: Table ======
    @FXML private TableView<Solder> tblSolders;
    @FXML private TableColumn<Solder, Number> colSTT, colDaysLeft;
    @FXML private TableColumn<Solder, String> colCode, colMaker, colLot;
    @FXML private TableColumn<Solder, LocalDate> colReceived, colMfg, colExpiry;
    @FXML private TableColumn<Solder, Double> colViscotester;
    @FXML private TableColumn<Solder, String> colRemark;


    // ====== FXML: Filters ======
    @FXML private TextField tfSearch, tfLot;
    @FXML private ComboBox<String> cbMaker;
    @FXML private DatePicker dpRecvFrom, dpRecvTo, dpExpFrom, dpExpTo;
    @FXML private CheckBox chkExpSoon;
    @FXML private Spinner<Integer> spExpDays;
    @FXML private Button btnRefresh, btnClear, btnImport, btnExport;

    // ====== Footer ======
    @FXML private Label lblTotal;

    // ====== State & Services ======
    private final SolderService solderService;
    private List<Solder> master = new ArrayList<>();

    @Autowired
    public SolderManagerController(SolderService solderService) {
        this.solderService = solderService;
    }

    @FXML
    public void initialize() {
        setupColumns();
        setupFilters();
        setupClipboard();
        btnImport.setOnAction(e -> handleImportExcelBasic());
        loadFromDatabase();
        btnExport.setOnAction(e -> exportSolderExcel());
        setupRowContextMenu();
        setupRowShortcuts();
    }

    // ---------------- UI setup ----------------
    private void setupColumns() {
        colSTT.setCellValueFactory(cd ->
                Bindings.createIntegerBinding(() -> tblSolders.getItems().indexOf(cd.getValue()) + 1));

        colCode.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getCode()));
        colMaker.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getMaker()));
        colLot.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getLot()));

        colReceived.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getReceivedDate()));
        colMfg.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getMfgDate()));
        colExpiry.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getExpiryDate()));

        colDaysLeft.setCellValueFactory(cd -> {
            LocalDate exp = cd.getValue().getExpiryDate();
            if (exp == null) return new SimpleIntegerProperty(0); // hiển thị 0 nếu không có HSD
            long days = ChronoUnit.DAYS.between(LocalDate.now(), exp);
            return new SimpleIntegerProperty((int) days);
        });

        // tô màu cột DaysLeft: đỏ nếu quá hạn, cam nếu <= N ngày
        colDaysLeft.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                int d = item.intValue();
                setText(String.valueOf(d));
                setStyle("");
                if (d < 0) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (chkExpSoon.isSelected() && d <= spExpDays.getValue()) {
                    setStyle("-fx-text-fill: orange;");
                }
            }
        });

        // ====== EDITABLE SETTINGS ======
        tblSolders.setEditable(true);
        // chỉ visco được edit
        colViscotester.setEditable(true);
        // các cột khác không edit
        colCode.setEditable(false);
        colMaker.setEditable(false);
        colLot.setEditable(false);
        colReceived.setEditable(false);
        colMfg.setEditable(false);
        colExpiry.setEditable(false);
        colDaysLeft.setEditable(false);
        if (colRemark != null) colRemark.setEditable(false);

        // ====== VISCOTESTER ======
        colViscotester.setCellValueFactory(cd ->
                new SimpleObjectProperty<>(cd.getValue().getViscotester()));

        var doubleConverter = new javafx.util.StringConverter<Double>() {
            @Override public String toString(Double v) {
                return v == null ? "" : (Math.floor(v) == v ? String.valueOf(v.longValue()) : v.toString());
            }
            @Override public Double fromString(String s) {
                if (s == null) return null;
                String t = s.trim();
                if (t.isEmpty()) return null; // cho phép xóa -> null
                return Double.valueOf(t);
            }
        };

        // cho phép gõ trực tiếp + tô màu theo remark
        // cho phép gõ trực tiếp + tô màu theo remark
        colViscotester.setCellFactory(col ->
                new TextFieldTableCell<Solder, Double>(doubleConverter) {
                    @Override
                    public void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);

                        // canh phải số và reset style base
                        String base = "-fx-alignment: CENTER-RIGHT;";
                        setStyle(base);

                        if (empty) {
                            setText(null);
                            return;
                        }

                        Solder s = (getTableRow() == null) ? null : getTableRow().getItem();
                        String remark =  computeRemark(s); // "", "OK", "NG"

                        switch (remark) {
                            case "OK" -> setStyle(base + " -fx-background-color:#EAF8EA; -fx-text-fill:#156D28; -fx-font-weight:bold;");
                            case "NG" -> setStyle(base + " -fx-background-color:#FDE8E8; -fx-text-fill:#B00020; -fx-font-weight:bold;");
                            default    -> setStyle(base);
                        }
                    }
                }
        );


        // lưu DB khi commit + refresh để cập nhật màu + remark
        colViscotester.setOnEditCommit(ev -> {
            Solder row = ev.getRowValue();
            Double oldVal = row.getViscotester();
            Double newVal = ev.getNewValue();

            row.setViscotester(newVal);
            try {
                solderService.updateSolder(row);
                tblSolders.refresh(); // cập nhật lại màu + remark
            } catch (Exception ex) {
                row.setViscotester(oldVal);         // rollback UI nếu lỗi
                tblSolders.refresh();
                showError("Cập nhật Viscotester lỗi: " + ex.getMessage());
            }
        });

        // ====== REMARK ======
        if (colRemark != null) {
            colRemark.setCellValueFactory(cd ->
                    new SimpleObjectProperty<>(computeRemark(cd.getValue())));
            colRemark.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) { setText(null); setStyle(""); return; }
                    setText(item);
                    switch (item) {
                        case "OK" -> setStyle("-fx-background-color: #EAF8E8; -fx-text-fill: #156D28; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        case "NG" -> setStyle("-fx-background-color: #FDE8E8; -fx-text-fill: #B00020; -fx-font-weight: bold; -fx-alignment: CENTER;");
                        default    -> setStyle("-fx-alignment: CENTER;");
                    }
                }
            });
        }
    }


    private void setupFilters() {
        // spinner ngày sắp hết hạn: 1..365, mặc định 30
        spExpDays.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));

        // hành vi nút
        btnRefresh.setOnAction(e -> loadFromDatabase());
        btnClear.setOnAction(e -> {
            tfSearch.clear();
            tfLot.clear();
            cbMaker.setValue(null);
            dpRecvFrom.setValue(null);
            dpRecvTo.setValue(null);
            dpExpFrom.setValue(null);
            dpExpTo.setValue(null);
            chkExpSoon.setSelected(false);
            applyFilters();
        });

        // kích hoạt lọc
        tfSearch.setOnAction(e -> applyFilters());
        tfLot.setOnAction(e -> applyFilters());
        cbMaker.valueProperty().addListener((o, ov, nv) -> applyFilters());
        dpRecvFrom.valueProperty().addListener((o, ov, nv) -> applyFilters());
        dpRecvTo.valueProperty().addListener((o, ov, nv) -> applyFilters());
        dpExpFrom.valueProperty().addListener((o, ov, nv) -> applyFilters());
        dpExpTo.valueProperty().addListener((o, ov, nv) -> applyFilters());
        chkExpSoon.selectedProperty().addListener((o, ov, nv) -> applyFilters());
        spExpDays.valueProperty().addListener((o, ov, nv) -> applyFilters());

        // enter trên ô tìm nhanh
        tfSearch.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) applyFilters(); });
        tfLot.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) applyFilters(); });

        // import/export (tuỳ chọn: bạn có thể gắn xử lý sau)
        btnImport.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Import Excel: sẽ triển khai sau.").showAndWait());
        btnExport.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Export: sẽ triển khai sau.").showAndWait());
    }

    private void setupClipboard() {
        tblSolders.getSelectionModel().setCellSelectionEnabled(true);
        tblSolders.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tblSolders.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.C) {
                FxClipboardUtils.copySelectionToClipboard(tblSolders);
            }
        });
    }

    // ---------------- Data ----------------
    private void loadFromDatabase() {
        master = solderService.getAllSolders();
        rebuildFilterChoices();
        applyFilters();
    }

    private void rebuildFilterChoices() {
        List<String> makers = master.stream()
                .map(Solder::getMaker)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        cbMaker.getItems().setAll(makers);
    }

    private void applyFilters() {
        String kw = safe(tfSearch.getText());
        String maker = cbMaker.getValue();
        String lotLike = safe(tfLot.getText());
        LocalDate recvFrom = dpRecvFrom.getValue();
        LocalDate recvTo = dpRecvTo.getValue();
        LocalDate expFrom = dpExpFrom.getValue();
        LocalDate expTo = dpExpTo.getValue();

        boolean expSoonOn = chkExpSoon.isSelected();
        int soonDays = spExpDays.getValue() == null ? 30 : spExpDays.getValue();
        LocalDate today = LocalDate.now();

        List<Solder> filtered = master.stream()
                .filter(s -> {
                    // keyword: chứa trong code/maker/lot (case-insensitive)
                    if (!kw.isEmpty()) {
                        String code = safe(s.getCode());
                        String mk = safe(s.getMaker());
                        String lot = safe(s.getLot());
                        String low = kw.toLowerCase(Locale.ROOT);
                        if (!(code.contains(low) || mk.contains(low) || lot.contains(low))) return false;
                    }
                    // maker exact
                    if (maker != null && !maker.isBlank()) {
                        if (!maker.equalsIgnoreCase(safeRaw(s.getMaker()))) return false;
                    }
                    // lot like
                    if (!lotLike.isEmpty()) {
                        if (!safe(s.getLot()).contains(lotLike.toLowerCase(Locale.ROOT))) return false;
                    }
                    // received range
                    LocalDate recv = s.getReceivedDate();
                    if (recvFrom != null && (recv == null || recv.isBefore(recvFrom))) return false;
                    if (recvTo != null && (recv == null || recv.isAfter(recvTo))) return false;
                    // expiry range
                    LocalDate exp = s.getExpiryDate();
                    if (expFrom != null && (exp == null || exp.isBefore(expFrom))) return false;
                    if (expTo != null && (exp == null || exp.isAfter(expTo))) return false;
                    // exp soon
                    if (expSoonOn) {
                        if (exp == null) return false;
                        if (exp.isBefore(today)) return false;                 // đã quá hạn → không tính "sắp"
                        if (exp.isAfter(today.plusDays(soonDays))) return false; // quá xa
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Solder::getExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        ObservableList<Solder> items = FXCollections.observableArrayList(filtered);
        tblSolders.setItems(items);
        lblTotal.setText(String.valueOf(items.size()));
    }

    // ---------------- Utils ----------------
    private static String safe(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
    private static String safeRaw(String s) {
        return s == null ? "" : s.trim();
    }

    // =======================
// Import Excel cơ bản cho Solder
// =======================
    private void handleImportExcelBasic() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file Excel (.xlsx) nhập Solder");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(tblSolders.getScene().getWindow());
        if (file == null) return;

        importSolderExcelBasic(file);
        loadFromDatabase();  // refresh bảng
    }

    private void importSolderExcelBasic(File file) {
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        java.util.Set<String> seenInFile = new java.util.HashSet<>();

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
             org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {

            org.apache.poi.ss.usermodel.Sheet sh = wb.getSheetAt(0);
            if (sh == null) {
                showInfo("Kết quả Import", "Không tìm thấy sheet đầu tiên.");
                return;
            }

            // Bỏ qua header ở row 0
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                org.apache.poi.ss.usermodel.Row row = sh.getRow(r);
                if (row == null) continue;

                try {
                    String code = getCellString(row.getCell(0)).trim();
                    if (code.isEmpty()) {
                        skipped++; errors.add("Dòng " + (r + 1) + ": Thiếu Code"); continue;
                    }

                    // Trùng code ngay trong file
                    if (!seenInFile.add(code)) {
                        skipped++; errors.add("Dòng " + (r + 1) + ": Trùng Code trong file: " + code); continue;
                    }

                    String maker = getCellString(row.getCell(1)).trim();
                    String lot   = getCellString(row.getCell(2)).trim();
                    LocalDate received = getCellLocalDate(row.getCell(3));
                    LocalDate mfg      = getCellLocalDate(row.getCell(4));
                    LocalDate expiry   = getCellLocalDate(row.getCell(5));

                    // Đã tồn tại trong DB → bỏ qua
                    if (solderService.existsByCode(code)) {
                        skipped++; errors.add("Dòng " + (r + 1) + ": Code đã tồn tại: " + code); continue;
                    }

                    // Tạo & thêm (validate ngày nằm trong service.addSolder)
                    Solder s = new Solder();
                    s.setCode(code);
                    s.setMaker(maker);
                    s.setLot(lot);
                    s.setReceivedDate(received);
                    s.setMfgDate(mfg);
                    s.setExpiryDate(expiry);

                    solderService.addSolder(s);
                    imported++;

                } catch (Exception ex) {
                    skipped++; errors.add("Dòng " + (r + 1) + ": " + ex.getMessage());
                }
            }

            String header = "✅ Thành công: " + imported + " | ❌ Bỏ qua: " + skipped;
            StringBuilder body = new StringBuilder();
            errors.stream().limit(20).forEach(e -> body.append(e).append("\n"));
            if (errors.size() > 20) body.append("... và ").append(errors.size() - 20).append(" dòng khác.");
            showInfo("Kết quả Import", header + (body.length() > 0 ? "\n\n" + body : ""));

        } catch (Exception e) {
            showError("Lỗi khi đọc file: " + e.getMessage());
        }
    }

    // --- Helpers ngắn gọn ---
    private String getCellString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double v = cell.getNumericCellValue();
                    long lv = (long) v;
                    yield (v == lv) ? Long.toString(lv) : Double.toString(v);
                }
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    var ev = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    var cv = ev.evaluate(cell);
                    yield switch (cv.getCellType()) {
                        case STRING -> cv.getStringValue().trim();
                        case NUMERIC -> {
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                            } else {
                                double v = cv.getNumberValue();
                                long lv = (long) v;
                                yield (v == lv) ? Long.toString(lv) : Double.toString(v);
                            }
                        }
                        case BOOLEAN -> Boolean.toString(cv.getBooleanValue());
                        default -> "";
                    };
                } catch (Exception ex) { yield ""; }
            }
            default -> "";
        };
    }

    private LocalDate getCellLocalDate(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC &&
                    org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            String s = getCellString(cell);
            if (s.isBlank()) return null;
            String[] fmts = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy", "yyyy/MM/dd"};
            for (String f : fmts) {
                try { return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern(f)); }
                catch (Exception ignore) {}
            }
            return LocalDate.parse(s); // thử ISO
        } catch (Exception ex) {
            return null; // không parse được thì để null, service sẽ kiểm tra hợp lệ
        }
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : null);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }



    // nhỏ gọn cho Alert config
    private static class AlertExt extends Alert {
        AlertExt(AlertType type, String content) { super(type, content); }
        AlertExt with(java.util.function.Consumer<Alert> cfg) { cfg.accept(this); return this; }
    }

    private void setupRowContextMenu() {
        tblSolders.setRowFactory(tv -> {
            TableRow<Solder> row = new TableRow<>();

            MenuItem miEdit = new MenuItem("Cập nhật…");
            MenuItem miDelete = new MenuItem("Xoá…");
            ContextMenu menu = new ContextMenu(miEdit, miDelete);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu));

            miEdit.setOnAction(e -> {
                Solder s = row.getItem();
                if (s != null) openEditDialog(s);
            });

            miDelete.setOnAction(e -> {
                Solder s = row.getItem();
                if (s != null) deleteOne(s);
            });

            return row;
        });
    }

    private void setupRowShortcuts() {
        // Phím Delete để xoá nhanh
        tblSolders.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                Solder s = tblSolders.getSelectionModel().getSelectedItem();
                if (s != null) deleteOne(s);
            }
        });
    }
    private void openEditDialog(Solder origin) {
        // Lấy lại từ DB để tránh dữ liệu cũ (nếu cần)
        Solder current = solderService.getSolderById(origin.getSolderId());
        if (current == null) {
            alert(Alert.AlertType.ERROR, "Không tìm thấy bản ghi.");
            return;
        }

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Cập nhật Solder");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8); gp.setPadding(new javafx.geometry.Insets(10));

        TextField tfCode = new TextField(current.getCode() == null ? "" : current.getCode());
        TextField tfMaker = new TextField(current.getMaker() == null ? "" : current.getMaker());
        TextField tfLot = new TextField(current.getLot() == null ? "" : current.getLot());
        DatePicker dpRecv = new DatePicker(current.getReceivedDate());
        DatePicker dpMfg = new DatePicker(current.getMfgDate());
        DatePicker dpExp = new DatePicker(current.getExpiryDate());

        int r = 0;
        gp.addRow(r++, new Label("Code:"), tfCode);
        gp.addRow(r++, new Label("Maker:"), tfMaker);
        gp.addRow(r++, new Label("Lot:"), tfLot);
        gp.addRow(r++, new Label("Received:"), dpRecv);
        gp.addRow(r++, new Label("Mfg:"), dpMfg);
        gp.addRow(r++, new Label("Expiry:"), dpExp);

        dlg.getDialogPane().setContent(gp);

        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String code = tfCode.getText().trim();
                    if (code.isEmpty()) {
                        alert(Alert.AlertType.WARNING, "Code không được để trống.");
                        return;
                    }

                    current.setCode(code);
                    current.setMaker(tfMaker.getText().trim());
                    current.setLot(tfLot.getText().trim());
                    current.setReceivedDate(dpRecv.getValue());
                    current.setMfgDate(dpMfg.getValue());
                    current.setExpiryDate(dpExp.getValue());

                    solderService.updateSolder(current);
                    loadFromDatabase();
                    alert(Alert.AlertType.INFORMATION, "Đã cập nhật.");
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Cập nhật lỗi: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteOne(Solder s) {
        Alert cf = new Alert(Alert.AlertType.CONFIRMATION);
        cf.setTitle("Xoá Solder");
        cf.setHeaderText("Xoá bản ghi Code: " + s.getCode());
        cf.setContentText("Thao tác này không thể hoàn tác. Bạn chắc chứ?");
        cf.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        cf.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    solderService.deleteSolderById(s.getSolderId());
                    loadFromDatabase();
                    alert(Alert.AlertType.INFORMATION, "Đã xoá.");
                } catch (Exception ex) {
                    alert(Alert.AlertType.ERROR, "Xoá lỗi: " + ex.getMessage());
                }
            }
        });
    }

    // =======================
    // Export Excel cơ bản cho Solder (xuất các dòng đang hiển thị)
    // =======================
    private void exportSolderExcel() {
        ObservableList<Solder> items = tblSolders.getItems();
        if (items == null || items.isEmpty()) {
            alert(Alert.AlertType.INFORMATION, "Không có dữ liệu để xuất.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        fc.setInitialFileName("solders_" + java.time.LocalDate.now() + ".xlsx");
        File file = fc.showSaveDialog(tblSolders.getScene().getWindow());
        if (file == null) return;

        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sh = wb.createSheet("Solders");

            // Styles
            org.apache.poi.ss.usermodel.Font headFont = wb.createFont();
            headFont.setBold(true);
            org.apache.poi.ss.usermodel.CellStyle headStyle = wb.createCellStyle();
            headStyle.setFont(headFont);

            org.apache.poi.ss.usermodel.CellStyle dateStyle = wb.createCellStyle();
            short df = wb.createDataFormat().getFormat("yyyy-mm-dd");
            dateStyle.setDataFormat(df);

            // Header
            String[] headers = {"STT", "Code", "Maker", "Lot", "ReceivedDate", "MfgDate", "ExpiryDate", "DaysLeft"};
            org.apache.poi.ss.usermodel.Row h = sh.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                org.apache.poi.ss.usermodel.Cell cell = h.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(headStyle);
            }

            // Data
            int r = 1;
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 0; i < items.size(); i++) {
                Solder s = items.get(i);
                org.apache.poi.ss.usermodel.Row row = sh.createRow(r++);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(nvl(s.getCode()));
                row.createCell(2).setCellValue(nvl(s.getMaker()));
                row.createCell(3).setCellValue(nvl(s.getLot()));

                setDateCell(row, 4, s.getReceivedDate(), dateStyle);
                setDateCell(row, 5, s.getMfgDate(), dateStyle);
                setDateCell(row, 6, s.getExpiryDate(), dateStyle);

                Integer daysLeft = (s.getExpiryDate() == null) ? null
                        : (int) java.time.temporal.ChronoUnit.DAYS.between(today, s.getExpiryDate());
                if (daysLeft != null) row.createCell(7).setCellValue(daysLeft);
                else row.createCell(7).setBlank();
            }

            // Tối ưu hiển thị
            for (int c = 0; c < headers.length; c++) sh.autoSizeColumn(c);
            sh.createFreezePane(0, 1);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                wb.write(fos);
            }

            alert(Alert.AlertType.INFORMATION, "Đã xuất: " + file.getAbsolutePath());
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Xuất Excel lỗi: " + ex.getMessage());
        }
    }

    private void setDateCell(org.apache.poi.ss.usermodel.Row row, int col,
                             java.time.LocalDate date, org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        if (date != null) {
            cell.setCellValue(java.sql.Date.valueOf(date)); // Excel date
            cell.setCellStyle(style);
        } else {
            cell.setBlank();
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    // --- helper để tính OK/NG theo maker ---
    private String computeRemark(Solder s) {
        if (s == null) return "";
        Double v = s.getViscotester();
        if (v == null) return ""; // chưa nhập -> để trống
        String mk = s.getMaker() == null ? "" : s.getMaker().trim().toLowerCase(Locale.ROOT);

        boolean ok;
        if ("tamura".equals(mk)) {
            ok = (v >= 180 && v <= 220);
        } else {
            ok = (v >= 160 && v <= 200);
        }
        return ok ? "OK" : "NG";
    }


}
