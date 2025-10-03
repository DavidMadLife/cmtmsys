package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.StencilViewDto;
import org.chemtrovina.cmtmsys.model.Stencil;
import org.chemtrovina.cmtmsys.model.StencilTransfer;
import org.chemtrovina.cmtmsys.repository.base.StencilRepository;
import org.chemtrovina.cmtmsys.repository.base.StencilTransferRepository;
import org.chemtrovina.cmtmsys.service.base.ProductService;
import org.chemtrovina.cmtmsys.service.base.StencilService;
import org.chemtrovina.cmtmsys.utils.VersionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class StencilServiceImpl implements StencilService {

    private final ProductService productService;
    private final StencilRepository repository;
    private final StencilTransferRepository transferRepository;
    public StencilServiceImpl(StencilRepository repository,
                              ProductService productService,
                              StencilTransferRepository transferRepository) {
        this.repository = repository;
        this.productService = productService;
        this.transferRepository = transferRepository;
    }

    @Override
    @Transactional
    public void addStencil(Stencil stencil) {
        // 1) Validate cơ bản
        Objects.requireNonNull(stencil.getBarcode(), "barcode is required");
        Objects.requireNonNull(stencil.getVersionLabel(), "versionLabel is required");
        Objects.requireNonNull(stencil.getProductId(), "productId is required");

        // 2) Lấy toàn bộ stencil cùng product (có thể thêm FOR UPDATE ở repo nếu bạn hỗ trợ)
        List<Stencil> sameProduct = repository.findByProductId(stencil.getProductId());

        // 3) Tìm phiên bản lớn nhất hiện có (nếu có)
        String currentMax = sameProduct.stream()
                .map(Stencil::getVersionLabel)
                .filter(v -> v != null && !v.isBlank())
                .max(VersionUtils::compare)
                .orElse(null);

        // 4) Lưu stencil mới
        if (stencil.getStatus() == null || stencil.getStatus().isBlank()) {
            stencil.setStatus("NEW");
        }
        repository.add(stencil);

        // 5) Nếu version mới > versionMax cũ → LOCK toàn bộ version nhỏ hơn
        if (currentMax == null || VersionUtils.isGreater(stencil.getVersionLabel(), currentMax)) {
            List<Integer> toLockIds = new ArrayList<>();
            for (Stencil s : sameProduct) {
                String v = s.getVersionLabel();
                if (v == null || v.isBlank()) continue;
                if (VersionUtils.isLess(v, stencil.getVersionLabel())) {
                    // tránh lock lại chính nó (trường hợp đặc biệt nếu repo.add trả id ngay và sameProduct chứa nó)
                    if (s.getStencilId() != 0 && s.getStencilId() != stencil.getStencilId()) {
                        if (!"LOCKED".equalsIgnoreCase(s.getStatus())) {
                            toLockIds.add(s.getStencilId());
                        }
                    }
                }
            }
            if (!toLockIds.isEmpty()) {
                // nếu có bulk:
                // repository.updateStatusMany(toLockIds, "LOCKED");
                // nếu chưa có bulk:
                for (Integer id : toLockIds) {
                    repository.updateStatus(id, "LOCKED");
                }
            }
        }
    }

    // ===== phần còn lại giữ nguyên =====
    @Override public void updateStencil(Stencil stencil) { repository.update(stencil); }
    @Override public void deleteStencilById(int stencilId) { repository.deleteById(stencilId); }
    @Override public Stencil getStencilById(int stencilId) { return repository.findById(stencilId); }
    @Override public Stencil getStencilByBarcode(String barcode) { return repository.findByBarcode(barcode); }
    @Override public List<Stencil> getAllStencils() { return repository.findAll(); }
    @Override public List<Stencil> getStencilsByProductId(int productId) { return repository.findByProductId(productId); }
    @Override public List<Stencil> getStencilsByWarehouseId(Integer warehouseId) { return repository.findByWarehouseId(warehouseId); }
    @Override public boolean existsByBarcode(String barcode) { return repository.existsByBarcode(barcode); }
    @Override public Stencil getStencilByProductAndNo(int productId, String stencilNo) { return repository.findByProductAndStencilNo(productId, stencilNo); }
    @Override public void updateStatus(int stencilId, String status) { repository.updateStatus(stencilId, status); }
    @Override
    @Transactional
    public void transferWarehouse(int stencilId, Integer toWarehouseId) {
        // Lấy stencil trước khi chuyển
        Stencil s = repository.findById(stencilId);
        if (s == null) {
            throw new IllegalArgumentException("Stencil not found with id=" + stencilId);
        }

        Integer fromId = s.getCurrentWarehouseId();

        // Update DB
        repository.transferWarehouse(stencilId, toWarehouseId);

        // Ghi log
        StencilTransfer log = new StencilTransfer();
        log.setStencilId(stencilId);
        log.setBarcode(s.getBarcode());
        log.setFromWarehouseId(fromId);
        log.setToWarehouseId(toWarehouseId);
        log.setTransferDate(LocalDateTime.now());
        log.setPerformedBy("system"); // TODO: lấy từ user login/session
        log.setNote("Auto transfer by service");

        transferRepository.add(log);
    }

    @Override public List<StencilViewDto> getAllStencilViews() { return repository.findAllViews(); }
    @Override public List<StencilViewDto> searchViews(String keyword, String productCode, String status, String warehouse) {
        return repository.searchViews(keyword, productCode, status, warehouse);
    }
}
