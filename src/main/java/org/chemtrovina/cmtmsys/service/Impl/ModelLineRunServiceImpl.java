package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRunRepository;
import org.chemtrovina.cmtmsys.service.base.ModelLineRunService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelLineRunServiceImpl implements ModelLineRunService {

    private final ModelLineRunRepository repository;

    public ModelLineRunServiceImpl(ModelLineRunRepository repository) {
        this.repository = repository;
    }

    @Override
    public void startRun(ModelLineRun run) {
        repository.add(run);
    }

    @Override
    public void endRun(int runId) {
        repository.endRun(runId);
    }

    @Override
    public ModelLineRun getRunById(int runId) {
        return repository.findById(runId);
    }

    @Override
    public List<ModelLineRun> getRunsByModelLineId(int modelLineId) {
        return repository.findByModelLineId(modelLineId);
    }
}
