package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.IcTray;

import java.util.List;

public interface IcTrayService {

    IcTray getById(Long id);

    IcTray getByBarcode(String barcode);

    List<IcTray> getActiveByLocation(Long locationId);

    IcTray create(IcTray tray);

    IcTray move(String barcode, Long toLocationId, String user);

    IcTray changeQty(String barcode, Integer newQty, String user, String reason);

    IcTray programRom(String barcode, String user);

    IcTray split(String barcode, Integer splitQty, String user, String reason);

    void deactivate(String barcode);
}