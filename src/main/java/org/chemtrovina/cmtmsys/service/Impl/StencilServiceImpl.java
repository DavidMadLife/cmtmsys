package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Stencil;
import org.chemtrovina.cmtmsys.repository.base.StencilRepository;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.StencilService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StencilServiceImpl implements StencilService {

    private final ProductService productService;
    private final StencilRepository repository;

    public StencilServiceImpl(StencilRepository repository, ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    @Override
    public void addStencil(Stencil stencil) {
        repository.add(stencil);
    }

    @Override
    public void updateStencil(Stencil stencil) {
        repository.update(stencil);
    }

    @Override
    public void deleteStencilById(int stencilId) {
        repository.deleteById(stencilId);
    }

    @Override
    public Stencil getStencilById(int stencilId) {
        return repository.findById(stencilId);
    }

    @Override
    public Stencil getStencilByBarcode(String barcode) {
        return repository.findByBarcode(barcode);
    }

    @Override
    public List<Stencil> getAllStencils() {
        return repository.findAll();
    }

    @Override
    public List<Stencil> getStencilsByProductId(int productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public List<Stencil> getStencilsByWarehouseId(Integer warehouseId) {
        return repository.findByWarehouseId(warehouseId);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return repository.existsByBarcode(barcode);
    }

    @Override
    public Stencil getStencilByProductAndNo(int productId, String stencilNo) {
        return repository.findByProductAndStencilNo(productId, stencilNo);
    }

    @Override
    public void updateStatus(int stencilId, String status) {
        repository.updateStatus(stencilId, status);
    }

    @Override
    public void transferWarehouse(int stencilId, Integer toWarehouseId) {
        repository.transferWarehouse(stencilId, toWarehouseId);
    }


    @Override
    public List<StencilViewDto> getAllStencilViews() {
        return repository.findAllViews();
    }

    @Override
    public List<StencilViewDto> searchViews(String keyword, String productCode, String status, String warehouse) {
        return repository.searchViews(keyword, productCode, status, warehouse);
    }



}
