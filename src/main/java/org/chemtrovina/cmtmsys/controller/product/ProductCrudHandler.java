package org.chemtrovina.cmtmsys.controller.product;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.model.enums.ModelType;
import org.chemtrovina.cmtmsys.service.base.ProductService;

public class ProductCrudHandler {

    private final ProductService productService;

    public ProductCrudHandler(ProductService productService) {
        this.productService = productService;
    }

    /* ================= CREATE ================= */
    public void createProduct(
            TextField txtCode,
            TextField txtName,
            ComboBox<String> cbType,
            Runnable after) {

        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String type = cbType.getValue();

        if (code.isEmpty() || name.isEmpty() || type == null) {
            alert("Nhập đủ Code, Name, Type.");
            return;
        }

        Product p = new Product();
        p.setProductCode(code);
        p.setName(name);
        p.setDescription(name);
        p.setModelType(ModelType.valueOf(type));

        productService.addProduct(p);
        alert("Tạo thành công.");

        txtCode.clear();
        txtName.clear();
        cbType.getSelectionModel().clearSelection();

        after.run();
    }


    /* ================= UPDATE ================= */
    public void updateProductDialog(Product p, TextField txtProductCode, Runnable after) {

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Cập nhật sản phẩm");

        TextField code = new TextField(p.getProductCode());
        TextField name = new TextField(p.getName());
        TextField desc = new TextField(p.getDescription());

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TOP","BOT","SINGLE","BOTTOP","NONE");
        cbType.setValue(p.getModelType().name());

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.add(new Label("Code"), 0,0); g.add(code,1,0);
        g.add(new Label("Name"), 0,1); g.add(name,1,1);
        g.add(new Label("Desc"), 0,2); g.add(desc,1,2);
        g.add(new Label("Type"), 0,3); g.add(cbType,1,3);

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                p.setProductCode(code.getText().trim());
                p.setName(name.getText().trim());
                p.setDescription(desc.getText().trim());
                p.setModelType(ModelType.valueOf(cbType.getValue()));

                productService.updateProduct(p);
                alert("Đã cập nhật.");

                txtProductCode.setText(p.getProductCode());
                after.run();
            }
        });
    }


    /* ================= DELETE ================= */
    public void deleteProduct(Product p, Runnable after) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Xoá sản phẩm " + p.getProductCode() + " ?", ButtonType.OK, ButtonType.CANCEL);


        c.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                productService.deleteProductWithBOM(p.getProductId());
                alert("Đã xoá.");
                after.run();
            }
        });
    }


    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
