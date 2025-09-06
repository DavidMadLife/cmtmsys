package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.MaterialCartTree;

import java.util.List;

public interface MaterialCartTreeRepository {
    void insert(MaterialCartTree tree);
    List<MaterialCartTree> findByCartId(int cartId);
    MaterialCartTree findByCode(String treeCode);

    MaterialCartTree getById(int treeId);

    void deleteById(int treeId);

    void update(MaterialCartTree tree);

    List<MaterialCartTree> findByIds(List<Integer> ids);

    MaterialCartTree getTreeByCartIdAndTreeCode(int cartId, String treeCode);



}
