package org.chemtrovina.cmtmsys.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.chemtrovina.cmtmsys.config.DataSourceConfig;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.ProductBOM;
import org.chemtrovina.cmtmsys.repository.Impl.ProductBOMRepositoryImpl;
import org.chemtrovina.cmtmsys.repository.base.ProductBOMRepository;
import org.chemtrovina.cmtmsys.service.Impl.ProductBOMServiceImpl;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductController {

    @FXML private TextField txtProductCode;
    @FXML private Button btnLoad;
    @FXML private TableView<ProductBomDto> tblProductBOM;
    @FXML private TableColumn<ProductBomDto, String> colProductCode;
    @FXML private TableColumn<ProductBomDto, String> colSappn;
    @FXML private TableColumn<ProductBomDto, Double> colQuantity;
    @FXML private TableColumn<ProductBomDto, String> colCreatedDate;
    @FXML private TableColumn<ProductBomDto, String> colUpdatedDate;


    private ProductBOMService productBOMService;

    @FXML
    public void initialize() {
        setupService();
        setupTable();
        setupActions();
    }

    private void setupService() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getDataSource());
        ProductBOMRepository repository = new ProductBOMRepositoryImpl(jdbcTemplate);
        this.productBOMService = new ProductBOMServiceImpl(repository);
    }

    private void setupTable() {
        colProductCode.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colSappn.setCellValueFactory(new PropertyValueFactory<>("sappn"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colUpdatedDate.setCellValueFactory(new PropertyValueFactory<>("updatedDate"));
        tblProductBOM.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblProductBOM.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode().toString().equals("C")) {
                copySelectionToClipboard();
            }
        });
        // ✅ Cho phép chọn từng ô
        tblProductBOM.getSelectionModel().setCellSelectionEnabled(true);

        // ✅ Cho phép chọn nhiều ô (liên tiếp hoặc rời rạc)
        tblProductBOM.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void setupActions() {
        btnLoad.setOnAction(e -> onSearch());
    }

    private void onSearch() {
        String productCode = txtProductCode.getText().trim();
        if (productCode.isEmpty()) {
            showAlert("Vui lòng nhập mã thành phẩm (ProductCode).");
            return;
        }

        List<ProductBomDto> bomList = productBOMService.getBomDtoByProductCode(productCode);
        tblProductBOM.setItems(FXCollections.observableArrayList(bomList));
    }

    private void copySelectionToClipboard() {
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<TablePosition> positionList = tblProductBOM.getSelectionModel().getSelectedCells();

        int prevRow = -1;
        for (TablePosition position : positionList) {
            int row = position.getRow();
            int col = position.getColumn();

            Object cell = tblProductBOM.getColumns().get(col).getCellData(row);
            if (cell == null) {
                cell = "";
            }

            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            clipboardString.append(cell);
            prevRow = row;
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
