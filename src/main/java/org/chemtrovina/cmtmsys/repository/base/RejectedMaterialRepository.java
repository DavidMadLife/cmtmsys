package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.dto.RejectedMaterialDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;

import java.util.List;

public interface RejectedMaterialRepository {
    void add(RejectedMaterial rejectedMaterial);

    List<RejectedMaterial> findByWorkOrderId(int workOrderId);

    List<RejectedMaterial> findByWarehouseId(int warehouseId);

    List<RejectedMaterial> findByWorkOrderIdAndSapCode(int workOrderId, String sapCode);
    RejectedMaterial findSingleByWorkOrderIdAndSapCode(int workOrderId, String sapCode);
    void update(RejectedMaterial rejectedMaterial);
    List<RejectedMaterialDto> findAllDto();
    void updateNoteById(int id, String note);



}
