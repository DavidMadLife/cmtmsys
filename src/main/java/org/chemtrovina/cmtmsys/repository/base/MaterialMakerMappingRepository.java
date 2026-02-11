package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.MaterialMakerMapping;

import java.util.List;

public interface MaterialMakerMappingRepository {
    List<MaterialMakerMapping> findAll();
    MaterialMakerMapping findById(int id);
    MaterialMakerMapping findByRollCode(String rollCode);

    int insert(MaterialMakerMapping m);
    int update(MaterialMakerMapping m);
    int delete(int id);

    int deleteByRollCode(String rollCode);
    int truncate(); // optional: xoá sạch để import lại
}
