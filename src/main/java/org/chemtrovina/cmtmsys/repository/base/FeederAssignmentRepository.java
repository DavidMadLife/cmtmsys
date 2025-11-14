package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.FeederAssignment;
import java.util.List;

public interface FeederAssignmentRepository {
    void add(FeederAssignment assignment);
    List<FeederAssignment> findByRunId(int runId);
    FeederAssignment findByRunAndFeeder(int runId, int feederId);

    FeederAssignment findById(int assignmentId);

}
