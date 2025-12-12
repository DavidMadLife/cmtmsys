package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.Position;

import java.util.List;

public interface PositionRepository {
    void add(Position position);
    void update(Position position);
    void deleteById(int positionId);
    Position findById(int positionId);
    List<Position> findAll();
    String getName(int positionId);
}
