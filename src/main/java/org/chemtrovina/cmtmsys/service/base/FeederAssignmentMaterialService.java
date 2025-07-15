package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.FeederAssignmentMaterial;

import java.util.List;
import java.util.Map;

public interface FeederAssignmentMaterialService {
    void attachMaterial(int assignmentId, int materialId, boolean isSupplement, String note);
    void detachMaterial(int id);
    List<FeederAssignmentMaterial> getMaterialsByAssignment(int assignmentId);
    FeederAssignmentMaterial getActiveAssignmentByMaterial(int materialId);
    List<FeederAssignmentMaterial> getActiveByRunId(int runId);

    Map<Integer, List<FeederAssignmentMaterial>> getAllActiveByRunGrouped(int runId);






}
