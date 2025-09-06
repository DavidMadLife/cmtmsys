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
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.PcbPerformanceLogService;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.chemtrovina.cmtmsys.utils.FxExceptionHandler;

@Component
public class PerformanceLogController {

    @FXML private TextField txtFolderPath;
    @FXML private ComboBox<Warehouse> cbWarehouse;

    @FXML private Button btnChooseFolder;
    @FXML private Button btnProcessLog;
    @FXML private Button btnStopWatching;

    @FXML private TableView<PcbPerformanceLog> tblPerformanceLog;
    @FXML private TableColumn<PcbPerformanceLog, String> colCarrierId, colAoiCode, colFileName;
    @FXML private TableColumn<PcbPerformanceLog, Integer> colTotal, colNg;
    @FXML private TableColumn<PcbPerformanceLog, Double> colPerformance;
    @FXML private TableColumn<PcbPerformanceLog, LocalDateTime> colCreatedAt;
    @FXML private TextArea txtLog;

    private final ObservableList<PcbPerformanceLog> logList = FXCollections.observableArrayList();

    private final PcbPerformanceLogService logService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private Path logFolder;

    private volatile boolean watching = false;
    private WatchService watchService;
    private FxExceptionHandler errorHandler;

    @Autowired private AoiCsvLogParser logParser;
    @Autowired private ProductResolver productResolver;


    @Autowired
    public PerformanceLogController(PcbPerformanceLogService logService, WarehouseService warehouseService, ProductService productService) {
        this.logService = logService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @FXML
    public void initialize() {
        setupTable();
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        cbWarehouse.setItems(FXCollections.observableArrayList(warehouses));
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

        this.errorHandler = new FxExceptionHandler(this::appendLog);

        btnChooseFolder.setOnAction(e -> errorHandler.handle(this::chooseFolder));
        btnProcessLog.setOnAction(e -> errorHandler.handle(this::startWatching));
        btnStopWatching.setOnAction(e -> errorHandler.handle(this::stopWatching));

        btnStopWatching.setDisable(true); // Ẩn nút "Dừng" ban đầu
    }

    private void setupTable() {
        colCarrierId.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCarrierId()));
        colAoiCode.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAoiMachineCode()));
        colFileName.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLogFileName()));
        colTotal.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTotalModules()));
        colNg.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNgModules()));
        colPerformance.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPerformance()));
        colCreatedAt.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCreatedAt()));
        tblPerformanceLog.setItems(logList);
    }

    private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Chọn thư mục log từ máy AOI");
        File folder = chooser.showDialog(null);
        if (folder != null) {
            txtFolderPath.setText(folder.getAbsolutePath());
            logFolder = folder.toPath();
        }
    }

    private void startWatching() {
        if (watching) {
            appendLog("⚠️ Đang theo dõi thư mục rồi. Vui lòng dừng trước khi chạy lại.");
            return;
        }

        Warehouse selectedWarehouse = cbWarehouse.getValue();

        if (logFolder == null || selectedWarehouse == null) {
            appendLog("❌ Vui lòng chọn thư mục và line (warehouse) trước khi xử lý.");
            return;
        }

        watching = true;
        btnProcessLog.setDisable(true);
        btnStopWatching.setDisable(false);

        appendLog("▶️ Bắt đầu theo dõi thư mục: " + logFolder + " | Line: " + selectedWarehouse.getName());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                logFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                while (watching) {
                    WatchKey key = watchService.poll(500, TimeUnit.MILLISECONDS);
                    if (key == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                        Path fileName = (Path) event.context();
                        Path fullPath = logFolder.resolve(fileName);

                        TimeUnit.MILLISECONDS.sleep(500);

                        if (!fileName.toString().toLowerCase().endsWith(".csv")) continue;

                        appendLog("📥 Phát hiện file mới: " + fileName);

                        String carrierId = extractCarrierIdFromFile(fullPath);
                        if (carrierId == null) {
                            appendLog("❌ Không tìm thấy CarrierID trong file: " + fileName);
                            continue;
                        }

                        appendLog("🔍 Carrier ID: " + carrierId);

                        if (logService.isAlreadyProcessed(carrierId)) {
                            if (logService.isFileAlreadySaved(fileName.toString())) {
                                appendLog("⏩ File đã xử lý trước đó: " + fileName);
                                continue;
                            } else {
                                appendLog("⚠️ Carrier đã xử lý trước, nhưng tên file mới → vẫn xử lý: " + fileName);
                            }
                        }

                        Product product = productResolver.resolveFromCarrier(carrierId, this::appendLog);
                        if (product == null) {
                            appendLog("❌ Không tìm thấy sản phẩm cho Carrier: " + carrierId);
                            continue;
                        }

                        PcbPerformanceLog log = logParser.parse(
                                fullPath,
                                carrierId,
                                product,
                                selectedWarehouse.getWarehouseId(),
                                this::appendLog
                        );

                        if (log != null) {
                            try {
                                logService.saveLog(log);
                                Platform.runLater(() -> logList.add(log));
                                appendLog("✅ Đã xử lý và lưu log: " + log.getLogFileName() +
                                        " | Model: " + log.getProductId());
                            } catch (Exception e) {
                                appendLog("❌ Lỗi khi lưu log: " + e.getMessage());
                            }
                        } else {
                            appendLog("⚠️ File không tạo được log hợp lệ: " + fileName);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) break;
                }

                appendLog("⛔ Đã dừng theo dõi thư mục.");
            } catch (Exception ex) {
                appendLog("❌ Lỗi theo dõi thư mục: " + ex.getMessage());
            } finally {
                watching = false;
                Platform.runLater(() -> {
                    btnProcessLog.setDisable(false);
                    btnStopWatching.setDisable(true);
                });
            }
        });
    }


    private void stopWatching() {
        watching = false;
        try {
            if (watchService != null) watchService.close();
        } catch (IOException e) {
            appendLog("❌ Lỗi khi dừng WatchService: " + e.getMessage());
        }
        appendLog("🛑 Yêu cầu dừng theo dõi thư mục đã được gửi.");
    }

    /*private PcbPerformanceLog processLogFile(Path file, String carrierId, int warehouseId) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                appendLog("❌ File không có header: " + file.getFileName());
                return null;
            }

            String[] headers = headerLine.split(",");
            int idxCarrierId = -1, idxAoi = 0, idxJudgement = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                if (h.contains("carrier")) idxCarrierId = i;
                else if (h.contains("judgement")) idxJudgement = i;
            }

            if (idxCarrierId == -1 || idxAoi == -1 || idxJudgement == -1) {
                appendLog("❌ Không tìm thấy cột 'Judgement', 'CarrierID' hoặc 'AOI' trong header: " + file.getFileName());
                return null;
            }

            // 🔍 Resolve product từ carrierId
            Product product = resolveProductFromCarrier(carrierId);
            if (product == null) {
                appendLog("❌ Không tìm thấy sản phẩm cho Carrier: " + carrierId);
                return null;
            }

            String aoi = null;
            int total = 0, ng = 0;
            String line;
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] parts = line.split(",", -1);
                if (parts.length <= Math.max(idxJudgement, Math.max(idxCarrierId, idxAoi))) {
                    appendLog("⚠️ Bỏ qua dòng " + lineNum + " (số cột không đủ): " + line);
                    continue;
                }

                String judgement = parts[idxJudgement].trim().toUpperCase();
                if (aoi == null) aoi = parts[idxAoi].trim();

                if (!judgement.equals("OK") && !judgement.equals("PASS")) ng++;
                total++;
            }

            appendLog("📊 Tổng dòng hợp lệ: " + total + ", NG: " + ng);

            // ➕ Nếu là model đặc biệt: chia đôi total và NG
            if ("H01040056C".equalsIgnoreCase(product.getProductCode())) {
                appendLog("✂️ Model đặc biệt H01040056C: chia đôi total & NG");
                total = total / 2;
                ng = ng / 2;
            }

            if (total == 0) {
                appendLog("⚠️ File không có dữ liệu hợp lệ (sau chia đôi): " + file.getFileName());
                return null;
            }

            double performance = ((double) (total - ng) / total) * 100;

            return new PcbPerformanceLog(0,
                    product.getProductId(),
                    warehouseId,
                    carrierId,
                    aoi,
                    total,
                    ng,
                    performance,
                    file.getFileName().toString(),
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            appendLog("❌ Lỗi xử lý file " + file.getFileName() + ": " + e.getMessage());
            return null;
        }
    }*/

    /*private Product resolveProductFromCarrier(String carrierId) {
        if (carrierId == null || carrierId.isBlank()) return null;

        // Tìm tất cả sản phẩm có ProductCode là “chuỗi con” của carrierId,
        // đã ORDER BY độ dài giảm dần ở tầng repo để ưu tiên mã dài nhất
        List<Product> candidates = productService.getProductsByCodeContainedInText(carrierId);
        if (candidates == null || candidates.isEmpty()) return null;

        // 1) Ưu tiên BOTTOP
        Product p = candidates.stream()
                .filter(x -> x.getModelType() != null && x.getModelType() == ModelType.BOTTOP)
                .findFirst()
                // 2) Nếu không có BOTTOP → ưu tiên SINGLE
                .orElseGet(() -> candidates.stream()
                        .filter(x -> x.getModelType() != null && x.getModelType() == ModelType.SINGLE)
                        .findFirst()
                        // 3) Nếu vẫn không có → lấy phần tử đầu (do đã sắp xếp theo độ dài code)
                        .orElse(candidates.get(0)));

        // (tuỳ chọn) Log ra productCode match được để bạn kiểm tra
        appendLog("🔗 Match ProductCode: " + p.getProductCode() + " (modelType: " + p.getModelType() + ")");
        return p;
    }*/

    private String extractCarrierIdFromFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); // skip header
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) return parts[2].trim();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private void appendLog(String message) {
        Platform.runLater(() -> txtLog.appendText("[" + LocalDateTime.now() + "] " + message + "\n"));
    }
}
