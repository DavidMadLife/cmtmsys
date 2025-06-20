package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.Position;

import java.util.List;

public interface PositionService {
    void addPosition(Position position);
    void updatePosition(Position position);
    void deletePositionById(int positionId);
    Position getPositionById(int positionId);
    List<Position> getAllPositions();
}
