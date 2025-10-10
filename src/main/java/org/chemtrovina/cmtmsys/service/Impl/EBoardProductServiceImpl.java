package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.EBoardProduct;
import org.chemtrovina.cmtmsys.repository.base.EBoardProductRepository;
import org.chemtrovina.cmtmsys.service.base.EBoardProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EBoardProductServiceImpl implements EBoardProductService {

    private final EBoardProductRepository repository;

    public EBoardProductServiceImpl(EBoardProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addProduct(EBoardProduct product) {
        repository.add(product);
    }

    @Override
    public void updateProduct(EBoardProduct product) {
        repository.update(product);
    }

    @Override
    public void deleteProduct(int id) {
        repository.delete(id);
    }

    @Override
    public EBoardProduct getProductById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<EBoardProduct> getProductsBySet(int setId) {
        return repository.findBySet(setId);
    }

    @Override
    public List<EBoardProduct> getProductsBySetAndCircuit(int setId, String circuitType) {
        return repository.findBySetAndCircuit(setId, circuitType);
    }

    @Override
    public List<EBoardProduct> getAllProducts() {
        return repository.findAll();
    }
}
