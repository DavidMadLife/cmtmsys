package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.StencilTransfer;
import org.chemtrovina.cmtmsys.repository.base.StencilTransferRepository;
import org.chemtrovina.cmtmsys.service.base.StencilTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StencilTransferServiceImpl implements StencilTransferService {

    private final StencilTransferRepository repository;

    public StencilTransferServiceImpl(StencilTransferRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void logTransfer(StencilTransfer transfer) {
        repository.add(transfer);
    }

    @Override
    public List<StencilTransfer> getTransfersByStencilId(int stencilId) {
        return repository.findByStencilId(stencilId);
    }

    @Override
    public List<StencilTransfer> getAllTransfers() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void deleteTransfer(int transferId) {
        repository.deleteById(transferId);
    }
}
