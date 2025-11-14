package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.FeederAssignment;

public interface FeederAssignmentService {
    FeederAssignment assignFeeder(int runId, int feederId, String assignedBy);

    FeederAssignment getAssignment(int runId, int feederId);

    FeederAssignment getById(int assignmentId);
}
