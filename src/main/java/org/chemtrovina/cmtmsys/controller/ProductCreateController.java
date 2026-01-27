package org.chemtrovina.cmtmsys.controller;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.controller.product.ProductCrudHandler;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.INVENTORY,
        UserRole.SUBLEEDER
})

@Component
public class ProductCreateController {

    @FXML private TextField txtProductCode;
    @FXML private TextField txtProductName;
    @FXML private ComboBox<String> cbModelType;
    @FXML private Button btnCreate;
    @FXML private Button btnCancel;

    private final ProductCrudHandler crudHandler;

    @Autowired
    public ProductCreateController(ProductService productService) {
        this.crudHandler = new ProductCrudHandler(productService);
    }

    @FXML
    public void initialize() {
        cbModelType.setItems(
                FXCollections.observableArrayList(
                        "TOP", "BOT", "SINGLE", "BOTTOP", "NONE"
                )
        );

        btnCreate.setOnAction(e -> onCreate());
        btnCancel.setOnAction(e -> close());
    }

    private void onCreate() {
        crudHandler.createProduct(
                txtProductCode,
                txtProductName,
                cbModelType,
                this::close
        );
    }

    private void close() {
        Stage stage = (Stage) txtProductCode.getScene().getWindow();
        stage.close();
    }
}
