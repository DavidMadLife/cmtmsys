package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Position;
import org.chemtrovina.cmtmsys.repository.base.PositionRepository;
import org.chemtrovina.cmtmsys.service.base.PositionService;

import java.util.List;

public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    public PositionServiceImpl(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Override
    public void addPosition(Position position) {
        positionRepository.add(position);
    }

    @Override
    public void updatePosition(Position position) {
        positionRepository.update(position);
    }

    @Override
    public void deletePositionById(int positionId) {
        positionRepository.deleteById(positionId);
    }

    @Override
    public Position getPositionById(int positionId) {
        return positionRepository.findById(positionId);
    }

    @Override
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }
}
