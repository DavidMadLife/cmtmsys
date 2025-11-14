package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.FeederAssignment;
import org.chemtrovina.cmtmsys.repository.base.FeederAssignmentRepository;
import org.chemtrovina.cmtmsys.service.base.FeederAssignmentService;
import org.springframework.stereotype.Service;

@Service
public class FeederAssignmentServiceImpl implements FeederAssignmentService {

    private final FeederAssignmentRepository repository;

    public FeederAssignmentServiceImpl(FeederAssignmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public FeederAssignment assignFeeder(int runId, int feederId, String assignedBy) {
        FeederAssignment existing = repository.findByRunAndFeeder(runId, feederId);
        if (existing != null) return existing;

        FeederAssignment newAssign = new FeederAssignment();
        newAssign.setRunId(runId);
        newAssign.setFeederId(feederId);
        newAssign.setAssignedBy(assignedBy);
        repository.add(newAssign);

        return repository.findByRunAndFeeder(runId, feederId);
    }

    @Override
    public FeederAssignment getAssignment(int runId, int feederId) {
        return repository.findByRunAndFeeder(runId, feederId);
    }

    @Override
    public FeederAssignment getById(int assignmentId) {
        return repository.findById(assignmentId);
    }


}
