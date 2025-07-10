package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.FeederRoll;
import org.chemtrovina.cmtmsys.repository.base.FeederRollRepository;
import org.chemtrovina.cmtmsys.service.base.FeederRollService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeederRollServiceImpl implements FeederRollService {

    private final FeederRollRepository repository;

    public FeederRollServiceImpl(FeederRollRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachRoll(FeederRoll feederRoll) {
        repository.attachRoll(feederRoll);
    }

    @Override
    public void detachRoll(int feederId, int runId) {
        repository.detachRoll(feederId, runId);
    }

    @Override
    public List<FeederRoll> getActiveRollsByRun(int runId) {
        return repository.findActiveByRun(runId);
    }

    @Override
    public List<FeederRoll> getRollHistoryByFeeder(int feederId) {
        return repository.findAllByFeeder(feederId);
    }
}
