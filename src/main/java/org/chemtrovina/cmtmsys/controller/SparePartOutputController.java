package org.chemtrovina.cmtmsys.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.chemtrovina.cmtmsys.model.SparePart;
import org.chemtrovina.cmtmsys.model.SparePartOutput;
import org.chemtrovina.cmtmsys.service.base.SparePartOutputService;
import org.chemtrovina.cmtmsys.service.base.SparePartService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SparePartOutputController {

    // === Form nhập ===
    @FXML private TextField txtSparePartCode;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtIssuer;
    @FXML private TextField txtReceiver;
    @FXML private TextField txtLine;
    @FXML private TextField txtNote;
    @FXML private Button btnAdd;
    @FXML private Button btnClear;

    // === Bảng dữ liệu ===
    @FXML private TableView<SparePartOutput> tblOutputs;
    @FXML private TableColumn<SparePartOutput, Integer> colDay;
    @FXML private TableColumn<SparePartOutput, Integer> colMonth;
    @FXML private TableColumn<SparePartOutput, Integer> colYear;
    @FXML private TableColumn<SparePartOutput, String> colName;
    @FXML private TableColumn<SparePartOutput, String> colCode;
    @FXML private TableColumn<SparePartOutput, byte[]> colImage;
    @FXML private TableColumn<SparePartOutput, String> colModel;
    @FXML private TableColumn<SparePartOutput, String> colSerial;
    @FXML private TableColumn<SparePartOutput, Integer> colQuantity;
    @FXML private TableColumn<SparePartOutput, String> colIssuer;
    @FXML private TableColumn<SparePartOutput, String> colReceiver;
    @FXML private TableColumn<SparePartOutput, String> colLine;
    @FXML private TableColumn<SparePartOutput, String> colNote;

    private final SparePartService sparePartService;
    private final SparePartOutputService outputService;

    public SparePartOutputController(SparePartService sparePartService,
                                     SparePartOutputService outputService) {
        this.sparePartService = sparePartService;
        this.outputService = outputService;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadOutputs();

        btnAdd.setOnAction(e -> onAdd());
        btnClear.setOnAction(e -> clearForm());
    }

    private void setupTable() {
        colDay.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOutputDate().getDayOfMonth()).asObject());
        colMonth.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOutputDate().getMonthValue()).asObject());
        colYear.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getOutputDate().getYear()).asObject());
        colQuantity.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());

        colIssuer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIssuer()));
        colReceiver.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReceiver()));
        colLine.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLine()));
        colNote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote()));

        // Các cột lấy từ JOIN SparePart
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSparePartName()));
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSparePartCode()));
        colModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colSerial.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSerial()));

        // Ảnh thu nhỏ
        colImage.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getImageData()));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(70);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(byte[] img, boolean empty) {
                super.updateItem(img, empty);
                if (empty || img == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image(new ByteArrayInputStream(img)));
                    setGraphic(imageView);
                }
            }
        });
    }

    private void loadOutputs() {
        List<SparePartOutput> list = outputService.getAllOutputsWithSparePart();
        tblOutputs.setItems(FXCollections.observableArrayList(list));
    }

    private void onAdd() {
        try {
            String code = txtSparePartCode.getText().trim();
            if (code.isEmpty()) {
                showAlert("⚠️ Vui lòng nhập mã phụ tùng!");
                return;
            }

            SparePart part = sparePartService.findByCode(code);
            if (part == null) {
                showAlert("❌ Không tìm thấy phụ tùng có mã: " + code);
                return;
            }

            SparePartOutput output = new SparePartOutput();
            output.setSparePartId(part.getId());
            output.setOutputDate(LocalDateTime.now());
            output.setQuantity(Integer.parseInt(txtQuantity.getText()));
            output.setIssuer(txtIssuer.getText());
            output.setReceiver(txtReceiver.getText());
            output.setLine(txtLine.getText());
            output.setNote(txtNote.getText());

            outputService.addOutput(output);
            showAlert("✅ Đã ghi nhận xuất phụ tùng!");
            loadOutputs();
            clearForm();

        } catch (Exception e) {
            showAlert("❌ Lỗi khi xuất phụ tùng: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtSparePartCode.clear();
        txtQuantity.clear();
        txtIssuer.clear();
        txtReceiver.clear();
        txtLine.clear();
        txtNote.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
