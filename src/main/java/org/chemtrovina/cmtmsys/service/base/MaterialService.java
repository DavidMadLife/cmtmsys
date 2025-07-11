package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.dto.MaterialDto;
import org.chemtrovina.cmtmsys.model.Material;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface MaterialService {
    void addMaterial(Material material);
    void updateMaterial(Material material);
    void deleteMaterialById(int id);
    Material getMaterialById(int id);
    Material getMaterialByRollCode(String rollCode);
    List<Material> getAllMaterials();
    List<Material> getMaterialsByWarehouse(int warehouseId);
    List<MaterialDto> getAllMaterialDtos();
    public Material transferMaterial(String barcode, String employeeId, int targetWarehouseId);
    public void importMaterialsFromExcel(File file, String employeeId);

    List<MaterialDto> searchMaterials(String sapCode, String barCode,LocalDateTime fromDate, LocalDateTime toDate, Integer warehouseId);

    List<Material> getMaterialsByIds(Set<Integer> ids);





}
