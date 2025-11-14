package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ModelLineRun;

import java.time.LocalDate;
import java.util.List;

public interface ModelLineRunRepository {
    void add(ModelLineRun run);
    void update(ModelLineRun run);
    ModelLineRun findById(int runId);
    List<ModelLineRun> findByModelLineId(int modelLineId);
    void markRunsAsDuplicate(int modelLineId, LocalDate date);
    ModelLineRun findLatestRunByModelLineId(int modelLineId);

    ModelLineRun findActiveRunByModelLineId(int modelLineId);


}
