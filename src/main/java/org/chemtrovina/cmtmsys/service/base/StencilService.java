package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Stencil;

import java.util.List;

public interface StencilService {
    void addStencil(Stencil stencil);
    void updateStencil(Stencil stencil);
    void deleteStencilById(int stencilId);

    Stencil getStencilById(int stencilId);
    Stencil getStencilByBarcode(String barcode);

    List<Stencil> getAllStencils();
    List<Stencil> getStencilsByProductId(int productId);
    List<Stencil> getStencilsByWarehouseId(Integer warehouseId);

    boolean existsByBarcode(String barcode);
    Stencil getStencilByProductAndNo(int productId, String stencilNo);

    void updateStatus(int stencilId, String status);
    void transferWarehouse(int stencilId, Integer toWarehouseId);

    List<StencilViewDto> getAllStencilViews();

    List<StencilViewDto> searchViews(String keyword, String productCode, String status, String warehouse);

}
