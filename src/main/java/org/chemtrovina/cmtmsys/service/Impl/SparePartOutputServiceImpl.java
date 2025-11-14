package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.SparePartOutput;
import org.chemtrovina.cmtmsys.repository.base.SparePartOutputRepository;
import org.chemtrovina.cmtmsys.service.base.SparePartOutputService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SparePartOutputServiceImpl implements SparePartOutputService {
    private final SparePartOutputRepository repository;

    public SparePartOutputServiceImpl(SparePartOutputRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addOutput(SparePartOutput output) {
        repository.add(output);
    }

    @Override
    public List<SparePartOutput> getAllOutputsWithSparePart() {
        return repository.findAll();
    }
}
