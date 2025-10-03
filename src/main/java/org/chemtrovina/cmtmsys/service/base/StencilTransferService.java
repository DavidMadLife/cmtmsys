package org.chemtrovina.cmtmsys.service.base;

import org.chemtrovina.cmtmsys.model.StencilTransfer;

import java.util.List;

public interface StencilTransferService {
    void logTransfer(StencilTransfer transfer);
    List<StencilTransfer> getTransfersByStencilId(int stencilId);
    List<StencilTransfer> getAllTransfers();
    void deleteTransfer(int transferId); // optional, nếu không muốn xoá log có thể bỏ
}
