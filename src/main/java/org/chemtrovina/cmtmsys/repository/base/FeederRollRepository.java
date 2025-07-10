package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.FeederRoll;

import java.util.List;

public interface FeederRollRepository {
    void attachRoll(FeederRoll feederRoll);
    void detachRoll(int feederId, int runId);
    List<FeederRoll> findActiveByRun(int runId);
    List<FeederRoll> findAllByFeeder(int feederId);
}
