package org.chemtrovina.cmtmsys.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.chemtrovina.cmtmsys.helper.AoiEBoardCsvLogParser;
import org.chemtrovina.cmtmsys.model.*;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.*;
import org.chemtrovina.cmtmsys.utils.FxExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class EBoardPerformanceController {

    @FXML private TextField txtFolderPath;
    @FXML private ComboBox<Warehouse> cbWarehouse;
    @FXML private Button btnChooseFolder;
    @FXML private Button btnStartWatch;
    @FXML private Button btnStopWatch;
    @FXML private TableView<EBoardPerformanceLog> tblLogs;
    @FXML private TableColumn<EBoardPerformanceLog, String> colSet, colCircuit, colModel, colAOI, colFile;
    @FXML private TableColumn<EBoardPerformanceLog, Integer> colTotal, colNg;
    @FXML private TableColumn<EBoardPerformanceLog, Double> colPerf;
    @FXML private TextArea txtLog;

    private final ObservableList<EBoardPerformanceLog> logList = FXCollections.observableArrayList();

    private final EBoardPerformanceLogService eboardLogService;
    private final EBoardSetService eboardSetService;
    private final EBoardProductService eboardProductService;
    private final WarehouseService warehouseService;
    private final AoiEBoardCsvLogParser eboardParser;
    private FxExceptionHandler errorHandler;

    private volatile boolean watching = false;
    private Path watchFolder;
    private WatchService watchService;

    @Autowired
    public EBoardPerformanceController(
            EBoardPerformanceLogService eboardLogService,
            EBoardSetService eboardSetService,
            EBoardProductService eboardProductService,
            WarehouseService warehouseService,
            AoiEBoardCsvLogParser eboardParser) {
        this.eboardLogService = eboardLogService;
        this.eboardSetService = eboardSetService;
        this.eboardProductService = eboardProductService;
        this.warehouseService = warehouseService;
        this.eboardParser = eboardParser;
    }

    @FXML
    public void initialize() {
        setupTable();

        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        cbWarehouse.setItems(FXCollections.observableArrayList(warehouses));
        cbWarehouse.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        this.errorHandler = new FxExceptionHandler(this::appendLog);
        btnChooseFolder.setOnAction(e -> errorHandler.handle(this::chooseFolder));
        btnStartWatch.setOnAction(e -> errorHandler.handle(this::startWatching));
        btnStopWatch.setOnAction(e -> errorHandler.handle(this::stopWatching));

        btnStopWatch.setDisable(true);
    }

    private void setupTable() {
        colSet.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.valueOf(data.getValue().getSetId())
        ));
        colCircuit.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCircuitType()));
        colModel.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getModelType()));
        colAOI.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAoiMachineCode()));
        colFile.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLogFileName()));
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalModules()));
        colNg.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNgModules()));
        colPerf.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPerformance()));
        tblLogs.setItems(logList);
    }

    private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Chọn thư mục log AOI E-Board");
        File folder = chooser.showDialog(null);
        if (folder != null) {
            txtFolderPath.setText(folder.getAbsolutePath());
            watchFolder = folder.toPath();
        }
    }

    private void startWatching() {
        if (watching) {
            appendLog("⚠️ Đang theo dõi rồi!");
            return;
        }

        if (watchFolder == null || cbWarehouse.getValue() == null) {
            appendLog("❌ Chưa chọn thư mục hoặc line!");
            return;
        }

        watching = true;
        btnStartWatch.setDisable(true);
        btnStopWatch.setDisable(false);
        appendLog("▶️ Theo dõi: " + watchFolder);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                watchFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                while (watching) {
                    WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
                    if (key == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                        Path fileName = (Path) event.context();
                        Path fullPath = watchFolder.resolve(fileName);

                        if (!fileName.toString().toLowerCase().endsWith(".csv")) continue;
                        TimeUnit.MILLISECONDS.sleep(800);

                        appendLog("📥 Phát hiện file mới: " + fileName);
                        handleEBoardCsvFile(fullPath, cbWarehouse.getValue());
                    }
                    key.reset();
                }
            } catch (Exception e) {
                appendLog("❌ Lỗi watcher: " + e.getMessage());
            } finally {
                watching = false;
                Platform.runLater(() -> {
                    btnStartWatch.setDisable(false);
                    btnStopWatch.setDisable(true);
                });
            }
        });
    }

    private void stopWatching() {
        watching = false;
        try {
            if (watchService != null) watchService.close();
        } catch (IOException e) {
            appendLog("❌ Lỗi dừng watcher: " + e.getMessage());
        }
        appendLog("🛑 Đã dừng theo dõi thư mục.");
    }

    private void handleEBoardCsvFile(Path filePath, Warehouse warehouse) {
        String fileName = filePath.getFileName().toString();

        try (var reader = Files.newBufferedReader(filePath)) {
            reader.readLine(); // bỏ header
            String first = reader.readLine();
            if (first == null) return;

            String[] parts = first.split(",", -1);
            String recipe = parts.length >= 7 ? parts[6].trim() : "";
            if (recipe.isEmpty()) {
                appendLog("⚠️ Không có recipe trong file: " + fileName);
                return;
            }

            // Parse SetName + Circuit + ModelType
            String setName = extractSetName(recipe);
            String circuit = extractCircuit(recipe);
            String modelType = extractModelType(recipe);

            EBoardSet set = eboardSetService.getSetByName(setName);
            if (set == null) {
                appendLog("❌ Không tìm thấy Set: " + setName);
                return;
            }

            List<EBoardProduct> products = eboardProductService.getProductsBySetAndCircuit(set.getSetId(), circuit);
            if (products.isEmpty()) {
                appendLog("⚠️ Không có sản phẩm trong Set " + setName + " (" + circuit + ")");
                return;
            }

            EBoardPerformanceLog log = eboardParser.parse(
                    filePath,
                    set.getSetId(),
                    warehouse.getWarehouseId(),
                    products.get(0).getId(),
                    this::appendLog
            );

            if (log != null) {
                eboardLogService.saveLog(log);
                Platform.runLater(() -> logList.add(log));
                appendLog("✅ Lưu log E-Board thành công: " + setName + " | " + circuit + " | " + modelType);
            }
        } catch (Exception e) {
            appendLog("❌ Lỗi xử lý file " + fileName + ": " + e.getMessage());
        }
    }

    private String extractSetName(String recipe) {
        recipe = recipe.replace("TYPE", "").trim();
        String[] parts = recipe.split("_");
        return parts[0].trim();
    }

    private String extractCircuit(String recipe) {
        if (recipe.toUpperCase().contains("LED")) return "LED";
        if (recipe.toUpperCase().contains("PD")) return "PD";
        return "UNKNOWN";
    }

    private String extractModelType(String recipe) {
        recipe = recipe.toUpperCase();
        if (recipe.contains("BOT")) return "BOT";
        if (recipe.contains("TOP")) return "TOP";
        return "SINGLE";
    }

    private void appendLog(String msg) {
        Platform.runLater(() ->
                txtLog.appendText("[" + LocalDateTime.now() + "] " + msg + "\n")
        );
    }
}
