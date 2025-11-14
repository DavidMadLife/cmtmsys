package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.ModelLineRun;
import java.util.List;

public interface ModelLineRunService {
    ModelLineRun createRun(int modelLineId);
    void endRun(int runId);
    List<ModelLineRun> getRunsByModelLineId(int modelLineId);
    void reopenRun(int runId);

    ModelLineRun getActiveRun(int modelLineId);
}
