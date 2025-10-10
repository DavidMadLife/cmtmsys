package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.EBoardSet;
import org.chemtrovina.cmtmsys.repository.base.EBoardSetRepository;
import org.chemtrovina.cmtmsys.service.base.EBoardSetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EBoardSetServiceImpl implements EBoardSetService {

    private final EBoardSetRepository repository;

    public EBoardSetServiceImpl(EBoardSetRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addSet(EBoardSet set) {
        repository.add(set);
    }

    @Override
    public void updateSet(EBoardSet set) {
        repository.update(set);
    }

    @Override
    public void deleteSet(int setId) {
        repository.delete(setId);
    }

    @Override
    public EBoardSet getSetById(int setId) {
        return repository.findById(setId);
    }

    @Override
    public EBoardSet getSetByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public List<EBoardSet> getAllSets() {
        return repository.findAll();
    }
}
