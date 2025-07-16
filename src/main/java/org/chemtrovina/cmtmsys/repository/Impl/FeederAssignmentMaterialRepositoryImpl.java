package org.chemtrovina.cmtmsys.repository.Impl;

import org.chemtrovina.cmtmsys.model.FeederAssignmentMaterial;
import org.chemtrovina.cmtmsys.repository.RowMapper.FeederAssignmentMaterialRowMapper;
import org.chemtrovina.cmtmsys.repository.base.FeederAssignmentMaterialRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FeederAssignmentMaterialRepositoryImpl implements FeederAssignmentMaterialRepository {
    private final JdbcTemplate jdbc;

    public FeederAssignmentMaterialRepositoryImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void add(FeederAssignmentMaterial m) {
        String sql = "INSERT INTO FeederAssignmentMaterials (AssignmentID, MaterialID, IsSupplement, AttachedAt, Note) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, m.getAssignmentId(), m.getMaterialId(), m.isSupplement(), m.getAttachedAt(), m.getNote());
    }

    @Override
    public void detach(int id) {
        String sql = "UPDATE FeederAssignmentMaterials SET IsActive = 0, DetachedAt = GETDATE() WHERE Id = ?";
        jdbc.update(sql, id);
    }

    @Override
    public List<FeederAssignmentMaterial> findByAssignmentId(int assignmentId) {
        String sql = "SELECT * FROM FeederAssignmentMaterials WHERE AssignmentID = ?";
        return jdbc.query(sql, new FeederAssignmentMaterialRowMapper(), assignmentId);
    }

    @Override
    public FeederAssignmentMaterial findActiveByMaterialId(int materialId) {
        String sql = """
        SELECT * FROM FeederAssignmentMaterials
        WHERE MaterialID = ? AND IsActive = 1
    """;
        List<FeederAssignmentMaterial> list = jdbc.query(sql, new FeederAssignmentMaterialRowMapper(), materialId);
        return list.isEmpty() ? null : list.get(0);
    }
    @Override
    public List<FeederAssignmentMaterial> findActiveByRunId(int runId) {
        String sql = """
        SELECT fam.* 
        FROM FeederAssignmentMaterials fam
        JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
        WHERE fa.RunID = ? AND fam.IsActive = 1
    """;
        return jdbc.query(sql, new FeederAssignmentMaterialRowMapper(), runId);
    }

    @Override
    public Map<Integer, List<FeederAssignmentMaterial>> findAllActiveByRunGroupedByFeeder(int runId) {
        String sql = """
        SELECT fam.*, fa.FeederID 
        FROM FeederAssignmentMaterials fam
        JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
        WHERE fa.RunID = ? AND fam.IsActive = 1
    """;

        return jdbc.query(sql, rs -> {
            Map<Integer, List<FeederAssignmentMaterial>> result = new HashMap<>();
            FeederAssignmentMaterialRowMapper mapper = new FeederAssignmentMaterialRowMapper();

            while (rs.next()) {
                int feederId = rs.getInt("FeederID");
                FeederAssignmentMaterial mat = mapper.mapRow(rs, rs.getRow());

                result.computeIfAbsent(feederId, k -> new java.util.ArrayList<>()).add(mat);
            }

            return result;
        }, runId);
    }

    @Override
    public List<FeederAssignmentMaterial> findActiveByFeederId(int feederId) {
        String sql = """
        SELECT fam.*
        FROM FeederAssignmentMaterials fam
        JOIN FeederAssignments fa ON fam.AssignmentID = fa.AssignmentID
        WHERE fa.FeederID = ? AND fam.IsActive = 1
        ORDER BY fam.AttachedAt ASC
    """;
        return jdbc.query(sql, new FeederAssignmentMaterialRowMapper(), feederId);
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM FeederAssignmentMaterials WHERE Id = ?";
        jdbc.update(sql, id);
    }


}
