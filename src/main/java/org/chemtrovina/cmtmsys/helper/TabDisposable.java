package org.chemtrovina.cmtmsys.helper;

public interface TabDisposable {
    void onTabClose();   // stop timer/thread, remove listeners, shutdown executors...
}
