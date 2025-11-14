package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.SparePart;
import org.chemtrovina.cmtmsys.repository.base.SparePartRepository;
import org.chemtrovina.cmtmsys.service.base.SparePartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SparePartServiceImpl implements SparePartService {

    private final SparePartRepository repository;

    public SparePartServiceImpl(SparePartRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void addSparePart(SparePart sparePart) {
        if (sparePart == null) {
            throw new IllegalArgumentException("SparePart cannot be null");
        }
        repository.add(sparePart);
    }

    @Override
    @Transactional
    public void updateSparePart(SparePart sparePart) {
        if (sparePart == null || sparePart.getId() == 0) {
            throw new IllegalArgumentException("Invalid spare part to update");
        }
        repository.update(sparePart);
    }

    @Override
    @Transactional
    public void deleteSparePartById(int id) {
        repository.deleteById(id);
    }

    @Override
    public SparePart getSparePartById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<SparePart> getAllSpareParts() {
        return repository.findAll();
    }

    @Override
    public List<SparePart> findByName(String name) {
        if (name == null || name.isBlank()) {
            return repository.findAll();
        }
        return repository.findByName(name);
    }

    @Override
    public SparePart findByCode(String code) {
        return repository.findByCode(code);
    }


}
