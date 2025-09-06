package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.MaterialCartTree;
import org.chemtrovina.cmtmsys.repository.base.MaterialCartTreeRepository;
import org.chemtrovina.cmtmsys.service.base.MaterialCartTreeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialCartTreeServiceImpl implements MaterialCartTreeService {

    private final MaterialCartTreeRepository treeRepo;

    public MaterialCartTreeServiceImpl(MaterialCartTreeRepository treeRepo) {
        this.treeRepo = treeRepo;
    }

    @Override
    public void createTree(MaterialCartTree tree) {
        treeRepo.insert(tree);
    }

    @Override
    public List<MaterialCartTree> getTreesByCartId(int cartId) {
        return treeRepo.findByCartId(cartId);
    }

    @Override
    public MaterialCartTree getTreeByCode(String treeCode) {
        return treeRepo.findByCode(treeCode);
    }

    @Override
    public MaterialCartTree getById(int treeId) {
        return treeRepo.getById(treeId);
    }

    @Override
    public MaterialCartTree addTreeToCart(int cartId, String treeCode) {
        MaterialCartTree tree = new MaterialCartTree();
        tree.setCartId(cartId);
        tree.setTreeCode(treeCode);
        tree.setCreatedAt(LocalDateTime.now());

        treeRepo.insert(tree);

        // ✅ Trả về đúng cây mới tạo dựa trên cartId và treeCode
        return treeRepo.getTreeByCartIdAndTreeCode(cartId, treeCode);
    }


    @Override
    public void deleteTreeById(int treeId) {
        treeRepo.deleteById(treeId);
    }

    @Override
    public void updateTree(MaterialCartTree tree) {
        treeRepo.update(tree);
    }

    @Override
    public List<MaterialCartTree> getByIds(List<Integer> treeIds) {
        return treeRepo.findByIds(treeIds);
    }

    @Override
    public MaterialCartTree getTreeByCartIdAndTreeCode(int cartId, String treeCode) {
        return treeRepo.getTreeByCartIdAndTreeCode(cartId, treeCode);
    }


}
