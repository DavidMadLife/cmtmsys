package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.ModelLineRun;

import java.util.List;

public interface ModelLineRunRepository {
    void add(ModelLineRun run);
    void update(ModelLineRun run);
    void endRun(int runId);
    ModelLineRun findById(int runId);
    List<ModelLineRun> findByModelLineId(int modelLineId);
}
