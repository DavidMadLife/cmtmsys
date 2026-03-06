package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.IcTrayLog;

import java.util.List;

public interface IcTrayLogService {

    List<IcTrayLog> getByTrayId(Long trayId);

    IcTrayLog save(IcTrayLog log);
}