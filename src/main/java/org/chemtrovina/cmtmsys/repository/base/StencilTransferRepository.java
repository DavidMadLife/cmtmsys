package org.chemtrovina.cmtmsys.repository.base;

import org.chemtrovina.cmtmsys.model.StencilTransfer;

import java.util.List;

public interface StencilTransferRepository {
    void add(StencilTransfer transfer);
    List<StencilTransfer> findByStencilId(int stencilId);
    List<StencilTransfer> findAll();
    void deleteById(int transferId);
}
