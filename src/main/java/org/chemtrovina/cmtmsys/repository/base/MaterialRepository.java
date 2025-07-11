package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Material;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface MaterialRepository {
    void add(Material material);
    void update(Material material);
    void deleteById(int id);
    Material findById(int id);
    Material findByRollCode(String rollCode);
    List<Material> findAll();
    List<Material> findByWarehouseId(int warehouseId);
    List<Material> search(String sapCode, String barCode, LocalDateTime fromDate, LocalDateTime toDate, Integer warehouseId);
    void restore(int planItemId, int quantity);

    List<Material> findByIds(Set<Integer> ids);


}
