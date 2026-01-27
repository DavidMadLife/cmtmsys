package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.chemtrovina.cmtmsys.model.SparePart;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.SparePartService;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class SparePartController {

    // ==== Table ====
    @FXML private TableView<SparePart> tblSpareParts;
    @FXML private TableColumn<SparePart, Integer> colId;
    @FXML private TableColumn<SparePart, LocalDate> colDate;
    @FXML private TableColumn<SparePart, String> colName;
    @FXML private TableColumn<SparePart, byte[]> colImage;
    @FXML private TableColumn<SparePart, String> colCode;
    @FXML private TableColumn<SparePart, String> colSupplier;
    @FXML private TableColumn<SparePart, String> colManufacturer;
    @FXML private TableColumn<SparePart, Integer> colQuantity;
    @FXML private TableColumn<SparePart, String> colUnit;
    @FXML private TableColumn<SparePart, String> colKeeper;
    @FXML private TableColumn<SparePart, String> colNote;

    // ==== Form ====
    @FXML private TextField txtName, txtCode, txtSupplier, txtManufacturer, txtQuantity, txtUnit, txtKeeper, txtNote;
    @FXML private DatePicker dpDate;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnClear, btnChooseImage;
    @FXML private BorderPane imageDropArea;
    @FXML private Button btnPasteImage;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImageName;

    // ==== Fields ====
    private byte[] selectedImageData;
    private File selectedFile;

    private final SparePartService sparePartService;

    public SparePartController(SparePartService sparePartService) {
        this.sparePartService = sparePartService;
    }

    // ============================================================
    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupActions();
        setupImageArea();
        tblSpareParts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSpareParts.getSelectionModel().setCellSelectionEnabled(true);
        tblSpareParts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // ============================================================
    /** Cấu hình bảng hiển thị dữ liệu */
    private void setupTable() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDate()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colCode.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCode()));
        colSupplier.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSupplier()));
        colManufacturer.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getManufacturer()));
        colQuantity.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        colUnit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUnit()));
        colKeeper.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getWarehouseKeeper()));
        colNote.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNote()));
        colImage.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getImageData()));

        // ✅ hiển thị ảnh thumbnail trong bảng
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(byte[] data, boolean empty) {
                super.updateItem(data, empty);
                if (empty || data == null) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image(new ByteArrayInputStream(data)));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        tblSpareParts.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !tblSpareParts.getSelectionModel().isEmpty()) {
                fillForm(tblSpareParts.getSelectionModel().getSelectedItem());
            }
        });
    }

    // ============================================================
    /** Setup nút chức năng */
    private void setupActions() {
        btnAdd.setOnAction(e -> onAdd());
        btnUpdate.setOnAction(e -> onUpdate());
        btnDelete.setOnAction(e -> onDelete());
        btnClear.setOnAction(e -> clearForm());
        btnChooseImage.setOnAction(e -> onChooseImage());
        btnPasteImage.setOnAction(e -> pasteImageFromClipboard());
    }

    // ============================================================
    /** Setup vùng dán ảnh */
    private void setupImageArea() {
        // ✅ Kéo thả ảnh
        imageDropArea.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            e.consume();
        });
        imageDropArea.setOnDragDropped(e -> {
            var db = e.getDragboard();
            if (db.hasFiles()) loadImageFromFile(db.getFiles().get(0));
            e.setDropCompleted(true);
            e.consume();
        });

        // ✅ Dán ảnh bằng Ctrl + V
        imageDropArea.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V) pasteImageFromClipboard();
        });
    }

    // ============================================================
    private void loadData() {
        tblSpareParts.setItems(FXCollections.observableArrayList(sparePartService.getAllSpareParts()));
    }

    private void onAdd() {
        try {
            SparePart sp = getFormData();
            sparePartService.addSparePart(sp);
            showAlert("✅ Đã thêm phụ tùng mới!");
            loadData();
            clearForm();
        } catch (Exception ex) {
            showAlert("❌ Lỗi khi thêm: " + ex.getMessage());
        }
    }

    private void onUpdate() {
        SparePart selected = tblSpareParts.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Vui lòng chọn phụ tùng để cập nhật."); return; }
        try {
            SparePart sp = getFormData();
            sp.setId(selected.getId());
            sparePartService.updateSparePart(sp);
            showAlert("✅ Đã cập nhật thành công!");
            loadData();
        } catch (Exception ex) {
            showAlert("❌ Lỗi cập nhật: " + ex.getMessage());
        }
    }

    private void onDelete() {
        SparePart selected = tblSpareParts.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Chọn phụ tùng để xóa."); return; }
        sparePartService.deleteSparePartById(selected.getId());
        showAlert("🗑️ Đã xóa phụ tùng!");
        loadData();
    }

    // ============================================================
    /** Chọn ảnh từ file */
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh phụ tùng");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ảnh", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(null);
        if (file != null) loadImageFromFile(file);
    }

    private void loadImageFromFile(File file) {
        try {
            selectedFile = file;
            selectedImageData = Files.readAllBytes(file.toPath());
            imgPreview.setImage(new Image(file.toURI().toString()));
            lblImageName.setText(file.getName());
        } catch (IOException e) {
            showAlert("❌ Lỗi đọc ảnh: " + e.getMessage());
        }
    }

    // ============================================================
    /** Dán ảnh từ clipboard */
    private void pasteImageFromClipboard() {
        Clipboard cb = Clipboard.getSystemClipboard();
        if (cb.hasImage()) {
            Image fxImage = cb.getImage();
            imgPreview.setImage(fxImage);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                var buffered = SwingFXUtils.fromFXImage(fxImage, null);
                ImageIO.write(buffered, "png", out);
                selectedImageData = out.toByteArray();
                lblImageName.setText("📋 Ảnh dán từ clipboard");
            } catch (IOException e) {
                showAlert("❌ Lỗi khi chuyển ảnh: " + e.getMessage());
            }
        } else {
            showAlert("📋 Clipboard không có ảnh hợp lệ. Hãy chụp màn hình rồi Ctrl+V lại nhé!");
        }
    }

    // ============================================================
    private SparePart getFormData() {
        SparePart sp = new SparePart();
        sp.setDate(dpDate.getValue() != null ? dpDate.getValue() : LocalDate.now());
        sp.setName(txtName.getText());
        sp.setCode(txtCode.getText());
        sp.setSupplier(txtSupplier.getText());
        sp.setManufacturer(txtManufacturer.getText());
        sp.setQuantity(Integer.parseInt(txtQuantity.getText()));
        sp.setUnit(txtUnit.getText());
        sp.setWarehouseKeeper(txtKeeper.getText());
        sp.setNote(txtNote.getText());
        sp.setImageData(selectedImageData);
        return sp;
    }

    private void fillForm(SparePart sp) {
        txtName.setText(sp.getName());
        txtCode.setText(sp.getCode());
        txtSupplier.setText(sp.getSupplier());
        txtManufacturer.setText(sp.getManufacturer());
        txtQuantity.setText(String.valueOf(sp.getQuantity()));
        txtUnit.setText(sp.getUnit());
        txtKeeper.setText(sp.getWarehouseKeeper());
        txtNote.setText(sp.getNote());
        dpDate.setValue(sp.getDate());
        if (sp.getImageData() != null)
            imgPreview.setImage(new Image(new ByteArrayInputStream(sp.getImageData())));
        else imgPreview.setImage(null);
    }

    private void clearForm() {
        txtName.clear(); txtCode.clear(); txtSupplier.clear(); txtManufacturer.clear();
        txtQuantity.clear(); txtUnit.clear(); txtKeeper.clear(); txtNote.clear();
        dpDate.setValue(LocalDate.now());
        lblImageName.setText("Chưa chọn ảnh");
        selectedImageData = null;
        imgPreview.setImage(null);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
