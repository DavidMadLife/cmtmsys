package org.chemtrovina.cmtmsys.controller.product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.chemtrovina.cmtmsys.dto.ProductBomDto;
import org.chemtrovina.cmtmsys.model.Product;
import org.chemtrovina.cmtmsys.service.base.ProductBOMService;
import org.chemtrovina.cmtmsys.utils.FxAlertUtils;

import java.util.List;

public class ProductBomLoader {

    private final ProductBOMService productBOMService;

    public ProductBomLoader(ProductBOMService productBOMService) {
        this.productBOMService = productBOMService;
    }

    public void loadBom(
            TextField txtCode,
            TextField txtName,
            ComboBox<String> cbModelFilter,
            List<Product> allProducts,
            TableView<ProductBomDto> tbl) {

        String code      = (txtCode.getText() == null ? "" : txtCode.getText().trim());
        String name      = (txtName.getText() == null ? "" : txtName.getText().trim());
        String modelType = cbModelFilter.getValue();


        if (code.isEmpty() && !name.isEmpty()) {
            Product found = allProducts.stream()
                    .filter(p -> name.equalsIgnoreCase(p.getName()))
                    .findFirst().orElse(null);

            if (found != null) {
                code = found.getProductCode();
                txtCode.setText(code);
                if (modelType == null) {
                    cbModelFilter.setValue(found.getModelType().name());
                }
            }
        }

        if (code.isEmpty() || modelType == null) {
            FxAlertUtils.warning("⚠️ Nhập mã hoặc chọn model + kiểu model.");
            return;
        }

        List<ProductBomDto> dtos =
                productBOMService.getBomDtoByCodeAndModel(code, modelType);

        tbl.setItems(FXCollections.observableArrayList(dtos));
    }


}
