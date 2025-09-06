package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.MaterialCartTree;

import java.util.List;

public interface MaterialCartTreeService {
    void createTree(MaterialCartTree tree);
    List<MaterialCartTree> getTreesByCartId(int cartId);
    MaterialCartTree getTreeByCode(String treeCode);

    MaterialCartTree getById(int treeId);
    MaterialCartTree addTreeToCart(int cartId, String treeCode);

    void deleteTreeById(int treeId);
    void updateTree(MaterialCartTree tree);

    List<MaterialCartTree> getByIds(List<Integer> treeIds);

    MaterialCartTree getTreeByCartIdAndTreeCode(int cartId, String treeCode);




}
