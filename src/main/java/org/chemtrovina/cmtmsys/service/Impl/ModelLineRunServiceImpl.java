package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import org.chemtrovina.cmtmsys.repository.base.ModelLineRunRepository;
import org.chemtrovina.cmtmsys.service.base.ModelLineRunService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModelLineRunServiceImpl implements ModelLineRunService {

    private final ModelLineRunRepository repository;

    public ModelLineRunServiceImpl(ModelLineRunRepository repository) {
        this.repository = repository;
    }

    @Override
    public ModelLineRun createRun(int modelLineId) {
        // Cập nhật tất cả run trong cùng ngày của modelLine này thành "Duplicate"
        repository.markRunsAsDuplicate(modelLineId, LocalDate.now());

        // Tạo run mới
        ModelLineRun newRun = new ModelLineRun();
        newRun.setModelLineId(modelLineId);
        newRun.setStartedAt(LocalDateTime.now());
        newRun.setStatus("Running");

        repository.add(newRun);

        // Trả về run vừa mới tạo
        return repository.findLatestRunByModelLineId(modelLineId);
    }


    @Override
    public void endRun(int runId) {
        ModelLineRun run = repository.findById(runId);
        if (run != null) {
            run.setStatus("Completed");
            run.setEndedAt(LocalDateTime.now());
            repository.update(run);
        }
    }

    @Override
    public List<ModelLineRun> getRunsByModelLineId(int modelLineId) {
        return repository.findByModelLineId(modelLineId);
    }
}
