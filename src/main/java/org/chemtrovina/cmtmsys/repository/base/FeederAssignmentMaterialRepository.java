package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.FeederAssignmentMaterial;
import java.util.List;
import java.util.Map;

public interface FeederAssignmentMaterialRepository {
    void add(FeederAssignmentMaterial material);
    void detach(int id);
    List<FeederAssignmentMaterial> findByAssignmentId(int assignmentId);
    FeederAssignmentMaterial findActiveByMaterialId(int materialId);
    List<FeederAssignmentMaterial> findActiveByRunId(int runId);
    Map<Integer, List<FeederAssignmentMaterial>> findAllActiveByRunGroupedByFeeder(int runId);


    List<FeederAssignmentMaterial> findActiveByFeederId(int feederId);

    void deleteById(int id);


    FeederAssignmentMaterial findLatestByMaterialId(int materialId);


}
