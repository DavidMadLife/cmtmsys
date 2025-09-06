package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.ProductCycleTimeViewDto;
import org.chemtrovina.cmtmsys.model.ProductCycleTime;
import org.chemtrovina.cmtmsys.model.enums.ModelType;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public interface ProductCycleTimeService {

    void add(ProductCycleTime pct);

    ProductCycleTime getActive(int productId, int warehouseId);

    List<ProductCycleTime> findHistory(int productId, int warehouseId);

    ProductCycleTime findById(int ctId);

    int deactivateAllFor(int productId, int warehouseId);

    int deleteById(int ctId);

    int deleteAllFor(int productId, int warehouseId);

    List<ProductCycleTime> listActiveForProduct(int productId);

    List<ProductCycleTime> listActiveForWarehouse(int warehouseId);

    /** Đặt cycle time mới làm active cho (productId, warehouseId) */
    void setActiveCycleTime(int productId, int warehouseId, BigDecimal ctSeconds, int array, String note);

    void importCycleTimesFromExcel(File file);

    List<ProductCycleTimeViewDto> searchView(String productCodeLike,
                                             ModelType modelTypeOrNull,
                                             String lineNameLike);
}
