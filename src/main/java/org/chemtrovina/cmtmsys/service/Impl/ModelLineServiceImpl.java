package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ModelLine;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRepository;
import org.chemtrovina.cmtmsys.service.base.ModelLineService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ModelLineServiceImpl implements ModelLineService {

    private final ModelLineRepository repository;

    public ModelLineServiceImpl(ModelLineRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addModelLine(ModelLine modelLine) {
        repository.add(modelLine);
    }

    @Override
    public void updateModelLine(ModelLine modelLine) {
        repository.update(modelLine);
    }

    @Override
    public void deleteModelLineById(int id) {
        repository.deleteById(id);
    }

    @Override
    public ModelLine getModelLineById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<ModelLine> getAllModelLines() {
        return repository.findAll();
    }

    @Override
    public ModelLine findOrCreateModelLine(int productId, int warehouseId) {
        return repository.findOrCreateModelLine(productId, warehouseId);
    }

}
