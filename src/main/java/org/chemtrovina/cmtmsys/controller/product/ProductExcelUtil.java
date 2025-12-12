package org.chemtrovina.cmtmsys.controller.product;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;


public class ProductExcelUtil {

    /**
     * Insert hoặc update Product + ProductBOM
     * (Thiết kế giống hệt logic bạn đang dùng trong ProductController)
     */
    public static void insertOrUpdate(
            JdbcTemplate jdbc,
            String productCode,
            String sappn,
            double quantity,
            String modelType
    ) {

        // ============================
        // 1. Lấy ProductID (tạo mới nếu chưa có)
        // ============================
        List<Integer> ids = jdbc.query(
                "SELECT productId FROM Products WHERE productCode = ?",
                new Object[]{productCode},
                (rs, i) -> rs.getInt(1)
        );

        int productId;

        if (ids.isEmpty()) {
            // creating new product
            jdbc.update("""
                        INSERT INTO Products (productCode, modelType, createdDate, updatedDate)
                        VALUES (?, ?, GETDATE(), GETDATE())
                    """,
                    productCode, modelType
            );

            productId = jdbc.queryForObject(
                    "SELECT productId FROM Products WHERE productCode = ?",
                    Integer.class,
                    productCode
            );
        } else {
            // update model type
            productId = ids.get(0);
            jdbc.update("""
                        UPDATE Products
                        SET modelType = ?, updatedDate = GETDATE()
                        WHERE productId = ?
                    """,
                    modelType, productId
            );
        }

        // ============================
        // 2. Insert/update ProductBOM
        // ============================
        List<Integer> exists = jdbc.query(
                "SELECT 1 FROM ProductBOM WHERE productId = ? AND sappn = ?",
                new Object[]{productId, sappn},
                (rs, i) -> rs.getInt(1)
        );

        if (exists.isEmpty()) {
            // insert new BOM row
            jdbc.update("""
                        INSERT INTO ProductBOM (productId, sappn, quantity, createdDate, updatedDate)
                        VALUES (?, ?, ?, GETDATE(), GETDATE())
                    """,
                    productId, sappn, quantity
            );
        } else {
            // update existing BOM
            jdbc.update("""
                        UPDATE ProductBOM
                        SET quantity = ?, updatedDate = GETDATE()
                        WHERE productId = ? AND sappn = ?
                    """,
                    quantity, productId, sappn
            );
        }
    }
}
