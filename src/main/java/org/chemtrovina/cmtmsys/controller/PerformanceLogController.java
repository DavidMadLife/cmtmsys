package org.chemtrovina.cmtmsys.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.chemtrovina.cmtmsys.helper.AoiCsvLogParser;
import org.chemtrovina.cmtmsys.helper.ProductResolver;
import org.chemtrovina.cmtmsys.model.PcbPerformanceLog;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.service.base.MaterialConsumeDetailLogService;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.chemtrovina.cmtmsys.utils.FxExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
public class PerformanceLogController {

    // ===================== UI =====================
    @FXML private TextField txtFolderPath;
    @FXML private ComboBox<Warehouse> cbWarehouse;
    @FXML private Button btnChooseFolder, btnProcessLog, btnStopWatching;

    @FXML private TableView<PcbPerformanceLog> tblPerformanceLog;
    @FXML private TableColumn<PcbPerformanceLog, String> colCarrierId, colAoiCode, colFileName;
    @FXML private TableColumn<PcbPerformanceLog, Integer> colTotal, colNg;
    @FXML private TableColumn<PcbPerformanceLog, Double> colPerformance;
    @FXML private TableColumn<PcbPerformanceLog, LocalDateTime> colCreatedAt;
    @FXML private TableColumn<PcbPerformanceLog, Double> colTimeDiff;


    @FXML private TextArea txtLog;

    // ===================== State =====================
    private final ObservableList<PcbPerformanceLog> logList = FXCollections.observableArrayList();

    private volatile boolean watching = false;
    private WatchService watchService;
    private Path logFolder;

    private final ScheduledExecutorService watcherExecutor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "AOI-Watcher");
                t.setDaemon(true);
                return t;
            });

    private FxExceptionHandler errorHandler;

    // ===================== Service =====================
    private final PcbPerformanceLogService logService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final MaterialConsumeDetailLogService consumeDetailService;

    @Autowired private AoiCsvLogParser logParser;
    @Autowired private ProductResolver productResolver;


    @Autowired
    public PerformanceLogController(
            PcbPerformanceLogService logService,
            WarehouseService warehouseService,
            ProductService productService,
            MaterialConsumeDetailLogService consumeDetailService) {

        this.logService = logService;
        this.warehouseService = warehouseService;
        this.productService = productService;
        this.consumeDetailService = consumeDetailService;
    }

    // ===================== INIT =====================
    @FXML
    public void initialize() {
        setupTableColumns();
        loadWarehouseCombo();
        setupButtons();

        errorHandler = new FxExceptionHandler(this::appendLog);
        btnStopWatching.setDisable(true);
    }

    // ===================== TABLE SETUP =====================
    private void setupTableColumns() {
        colCarrierId.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getCarrierId()));
        colAoiCode.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getAoiMachineCode()));
        colFileName.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getLogFileName()));
        colTotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotalModules()));
        colNg.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNgModules()));
        colPerformance.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getPerformance()));
        colCreatedAt.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCreatedAt()));
        colTimeDiff.setCellValueFactory(
                d -> new ReadOnlyObjectWrapper<>(d.getValue().getTimeDiffSeconds())
        );

        tblPerformanceLog.setItems(logList);
    }

    private void loadWarehouseCombo() {
        List<Warehouse> list = warehouseService.getAllWarehouses();
        cbWarehouse.setItems(FXCollections.observableArrayList(list));

        cbWarehouse.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? null : w.getName());
            }
        });

        cbWarehouse.setCellFactory(c -> new ListCell<>() {
            @Override protected void updateItem(Warehouse w, boolean empty) {
                super.updateItem(w, empty);
                setText(empty || w == null ? null : w.getName());
            }
        });
    }

    // ===================== BUTTON SETUP =====================
    private void setupButtons() {
        btnChooseFolder.setOnAction(e -> errorHandler.handle(this::chooseFolder));
        btnProcessLog.setOnAction(e -> errorHandler.handle(this::startWatching));
        btnStopWatching.setOnAction(e -> errorHandler.handle(this::stopWatching));
    }

    private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Chọn thư mục log AOI");

        File folder = chooser.showDialog(null);
        if (folder != null) {
            logFolder = folder.toPath();
            txtFolderPath.setText(logFolder.toString());
            appendLog("📂 Log folder selected: " + logFolder);
        }
    }

    // ===================== WATCH LOOP =====================
    private void startWatching() {
        if (!validateBeforeWatch()) return;

        watching = true;
        setWatchingUIState(true);

        appendLog("▶️ Watching folder: " + logFolder);

        watcherExecutor.scheduleWithFixedDelay(
                () -> watchFolder(cbWarehouse.getValue()),
                0, 300, TimeUnit.MILLISECONDS
        );
    }

    private boolean validateBeforeWatch() {
        if (watching) {
            appendLog("⚠️ Đang theo dõi.");
            return false;
        }
        if (logFolder == null) {
            appendLog("❌ Folder chưa chọn.");
            return false;
        }
        if (cbWarehouse.getValue() == null) {
            appendLog("❌ Warehouse chưa chọn.");
            return false;
        }
        return true;
    }

    private void watchFolder(Warehouse warehouse) {
        if (!watching) return;

        try {
            if (watchService == null)
                watchService = FileSystems.getDefault().newWatchService();

            logFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key = watchService.poll();
            if (key == null) return;

            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() != StandardWatchEventKinds.ENTRY_CREATE) continue;

                Path fileName = (Path) event.context();
                Path fullPath = logFolder.resolve(fileName);

                if (fileName.toString().toLowerCase().endsWith(".csv")) {
                    handleCsv(fullPath, warehouse);
                }
            }
            key.reset();

        } catch (Exception e) {
            appendLog("❌ Watcher error: " + e.getMessage());
        }
    }

    private void handleCsv(Path fullPath, Warehouse warehouse) {
        try {
            TimeUnit.MILLISECONDS.sleep(400); // ensure file copied fully

            String fileName = fullPath.getFileName().toString();
            appendLog("📥 New file detected: " + fileName);

            String carrierId = extractCarrier(fullPath);
            if (carrierId == null) {
                appendLog("❌ Cannot detect CarrierID.");
                return;
            }

            if (logService.isAlreadyProcessed(carrierId) &&
                    logService.isFileAlreadySaved(fileName)) {
                appendLog("⏩ Already processed.");
                return;
            }

            Product product = productResolver.resolveFromCarrierAndFileName(
                    carrierId, fileName, this::appendLog
            );

            if (product == null) {
                appendLog("❌ Cannot resolve Product.");
                return;
            }

            processLog(fullPath, carrierId, product, warehouse);

        } catch (Exception e) {
            appendLog("❌ Error: " + e.getMessage());
        }
    }

    private void processLog(Path fullPath, String carrierId, Product product, Warehouse warehouse) {
        try {
            PcbPerformanceLog log = logParser.parse(
                    fullPath, carrierId, product, warehouse.getWarehouseId(), this::appendLog
            );

            if (log == null) {
                appendLog("⚠️ Parse failed.");
                return;
            }

            logService.saveLog(log);
            Platform.runLater(() -> logList.add(log));

            appendLog("✅ Saved: " + log.getLogFileName());

            appendLog("⚙️ Consuming materials...");
            List<String> warnings = consumeDetailService.consumeByAoiLog(log);

            if (!warnings.isEmpty()) {
                appendLog("⛔ Shortages:");
                warnings.forEach(w -> appendLog("   - " + w));
            } else {
                appendLog("✅ Material consumption OK.");
            }

        } catch (Exception e) {
            appendLog("❌ Process log failed: " + e.getMessage());
        }
    }

    // ===================== Carrier Extract =====================
    private String extractCarrier(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file)) {
            br.readLine(); // skip header
            String ln = br.readLine();

            if (ln != null) {
                String[] parts = ln.split(",");
                if (parts.length >= 3) return parts[2].trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ===================== STOP =====================
    private void stopWatching() {
        watching = false;
        setWatchingUIState(false);

        try {
            if (watchService != null) watchService.close();
        } catch (Exception ignored) {}

        appendLog("🛑 Stopped.");
    }

    private void setWatchingUIState(boolean isWatching) {
        Platform.runLater(() -> {
            btnProcessLog.setDisable(isWatching);
            btnStopWatching.setDisable(!isWatching);
        });
    }

    // ===================== LOG =====================
    private void appendLog(String msg) {
        Platform.runLater(() ->
                txtLog.appendText("[" + LocalDateTime.now() + "] " + msg + "\n")
        );
    }
}
