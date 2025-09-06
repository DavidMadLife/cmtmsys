package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Stencil;

import java.util.List;

public interface StencilRepository {
    void add(Stencil stencil);
    void update(Stencil stencil);
    void deleteById(int stencilId);

    Stencil findById(int stencilId);
    Stencil findByBarcode(String barcode);

    List<Stencil> findAll();
    List<Stencil> findByProductId(int productId);
    List<Stencil> findByWarehouseId(Integer warehouseId); // cho phép null (đang trên máy)

    boolean existsByBarcode(String barcode);
    Stencil findByProductAndStencilNo(int productId, String stencilNo);

    // tiện ích nghiệp vụ
    void updateStatus(int stencilId, String status);
    void transferWarehouse(int stencilId, Integer toWarehouseId);

    List<StencilViewDto> findAllViews();

    List<StencilViewDto> searchViews(String keyword, String productCode, String status, String warehouse);

}
