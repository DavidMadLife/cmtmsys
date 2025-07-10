package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ModelLineRun;

import java.util.List;

public interface ModelLineRunService {
    void startRun(ModelLineRun run);
    void endRun(int runId);
    ModelLineRun getRunById(int runId);
    List<ModelLineRun> getRunsByModelLineId(int modelLineId);
}
