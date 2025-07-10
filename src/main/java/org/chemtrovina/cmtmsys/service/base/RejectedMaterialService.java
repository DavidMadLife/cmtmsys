package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.RejectedMaterialDto;
import org.chemtrovina.cmtmsys.model.RejectedMaterial;

import java.util.List;

public interface RejectedMaterialService {
    void addRejectedMaterial(RejectedMaterial rejectedMaterial);

    List<RejectedMaterial> getByWorkOrderId(int workOrderId);

    List<RejectedMaterial> getByWarehouseId(int warehouseId);

    List<RejectedMaterial> getByWorkOrderIdAndSapCode(int workOrderId, String sapCode);

    RejectedMaterial findByWorkOrderIdAndSapCode(int woId, String sapCode);
    void updateRejectedMaterial(RejectedMaterial rm);
    List<RejectedMaterialDto> getAllDto();
    void addOrUpdateRejectedMaterial(RejectedMaterial rm);

    void updateNoteById(int id, String note);


}
