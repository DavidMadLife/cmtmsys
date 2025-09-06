package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.ProductCycleTimeViewDto;
import org.chemtrovina.cmtmsys.model.ProductCycleTime;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.math.BigDecimal;
import java.util.List;

public interface ProductCycleTimeRepository {

    /** Insert 1 bản ghi (không tự deactivate) */
    void add(ProductCycleTime pct);

    /** Lấy bản active cho (productId, warehouseId) */
    ProductCycleTime getActive(int productId, int warehouseId);

    /** Lịch sử tất cả versions cho (productId, warehouseId) */
    List<ProductCycleTime> findHistory(int productId, int warehouseId);

    /** Tìm theo CtId */
    ProductCycleTime findById(int ctId);

    /** Set Active=0 cho tất cả bản active hiện tại của (productId, warehouseId) */
    int deactivateAllFor(int productId, int warehouseId);

    /** Xoá theo CtId */
    int deleteById(int ctId);

    /** Xoá tất cả theo (productId, warehouseId) */
    int deleteAllFor(int productId, int warehouseId);

    /** Danh sách bản active của 1 product trên mọi warehouse */
    List<ProductCycleTime> listActiveForProduct(int productId);

    /** Danh sách bản active của 1 warehouse cho mọi product */
    List<ProductCycleTime> listActiveForWarehouse(int warehouseId);

    /** Thiết lập CT mới làm active cho (productId, warehouseId), mặc định array=1 */
    void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, String note);

    /** Thiết lập CT mới làm active cho (productId, warehouseId) với array chỉ định */
    void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, int array, String note);

    List<ProductCycleTimeViewDto> searchView(String productCodeLike,
                                             ModelType modelTypeOrNull,
                                             String lineNameLike);
}
