package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.dto.RejectedMaterialDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;
import org.chemtrovina.cmtmsys.repository.base.RejectedMaterialRepository;
import org.chemtrovina.cmtmsys.service.base.RejectedMaterialService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RejectedMaterialServiceImpl implements RejectedMaterialService {

    private final RejectedMaterialRepository repository;

    public RejectedMaterialServiceImpl(RejectedMaterialRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addRejectedMaterial(RejectedMaterial rejectedMaterial) {
        repository.add(rejectedMaterial);
    }

    @Override
    public List<RejectedMaterial> getByWorkOrderId(int workOrderId) {
        return repository.findByWorkOrderId(workOrderId);
    }

    @Override
    public List<RejectedMaterial> getByWarehouseId(int warehouseId) {
        return repository.findByWarehouseId(warehouseId);
    }

    @Override
    public List<RejectedMaterial> getByWorkOrderIdAndSapCode(int workOrderId, String sapCode) {
        return repository.findByWorkOrderIdAndSapCode(workOrderId, sapCode);
    }

    @Override
    public RejectedMaterial findByWorkOrderIdAndSapCode(int woId, String sapCode) {
        return repository.findSingleByWorkOrderIdAndSapCode(woId, sapCode);
    }

    @Override
    public void updateRejectedMaterial(RejectedMaterial rm) {
        repository.update(rm);
    }

    @Override
    public List<RejectedMaterialDto> getAllDto() {
        return repository.findAllDto();
    }

    @Override
    public void addOrUpdateRejectedMaterial(RejectedMaterial rm) {
        RejectedMaterial existing = repository.findSingleByWorkOrderIdAndSapCode(rm.getWorkOrderId(), rm.getSapCode());
        if (existing != null) {
            rm.setId(existing.getId());
            rm.setQuantity(rm.getQuantity()); // ghi đè hoặc cộng dồn tùy bạn
            repository.update(rm);
        } else {
            repository.add(rm);
        }
    }

    @Override
    public void updateNoteById(int id, String note) {
        repository.updateNoteById(id, note);
    }


}
