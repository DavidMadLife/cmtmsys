package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.FeederRoll;

import java.util.List;

public interface FeederRollService {
    void attachRoll(FeederRoll feederRoll);
    void detachRoll(int feederId, int runId);
    List<FeederRoll> getActiveRollsByRun(int runId);
    List<FeederRoll> getRollHistoryByFeeder(int feederId);
}
