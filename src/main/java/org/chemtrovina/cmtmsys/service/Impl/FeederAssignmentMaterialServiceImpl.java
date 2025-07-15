package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.FeederAssignmentMaterial;
import org.chemtrovina.cmtmsys.repository.base.FeederAssignmentMaterialRepository;
import org.chemtrovina.cmtmsys.service.base.FeederAssignmentMaterialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeederAssignmentMaterialServiceImpl implements FeederAssignmentMaterialService {

    private final FeederAssignmentMaterialRepository repository;

    public FeederAssignmentMaterialServiceImpl(FeederAssignmentMaterialRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachMaterial(int assignmentId, int materialId, boolean isSupplement, String note) {
        FeederAssignmentMaterial mat = new FeederAssignmentMaterial();
        mat.setAssignmentId(assignmentId);
        mat.setMaterialId(materialId);
        mat.setSupplement(isSupplement);
        mat.setNote(note);
        repository.add(mat);
    }

    @Override
    public void detachMaterial(int id) {
        repository.detach(id);
    }

    @Override
    public List<FeederAssignmentMaterial> getMaterialsByAssignment(int assignmentId) {
        return repository.findByAssignmentId(assignmentId);
    }

    @Override
    public FeederAssignmentMaterial getActiveAssignmentByMaterial(int materialId) {
        return repository.findActiveByMaterialId(materialId);
    }

    @Override
    public List<FeederAssignmentMaterial> getActiveByRunId(int runId) {
        return repository.findActiveByRunId(runId);
    }

    @Override
    public Map<Integer, List<FeederAssignmentMaterial>> getAllActiveByRunGrouped(int runId) {
        return repository.findAllActiveByRunGroupedByFeeder(runId);
    }





}
