package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.WarehouseTransfer;
import org.chemtrovina.cmtmsys.model.WarehouseTransferDetail;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferDetailRepository;
import org.chemtrovina.cmtmsys.repository.base.WarehouseTransferRepository;
import org.chemtrovina.cmtmsys.service.base.WarehouseTransferService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class WarehouseTransferServiceImpl implements WarehouseTransferService {

    private final WarehouseTransferRepository transferRepository;
    private final WarehouseTransferDetailRepository detailRepository;

    public WarehouseTransferServiceImpl(WarehouseTransferRepository transferRepository,
                                        WarehouseTransferDetailRepository detailRepository) {
        this.transferRepository = transferRepository;
        this.detailRepository = detailRepository;
    }

    @Override
    public void createTransfer(WarehouseTransfer transfer, List<WarehouseTransferDetail> details) {
        transferRepository.add(transfer);

        // Retrieve generated ID if needed, else assume passed in
        List<WarehouseTransfer> all = transferRepository.getAll();
        int newId = all.get(all.size() - 1).getTransferId();  // Simplified assumption

        for (WarehouseTransferDetail detail : details) {
            detail.setTransferId(newId);
            detailRepository.add(detail);
        }
    }

    @Override
    public List<WarehouseTransfer> getAllTransfers() {
        return transferRepository.getAll();
    }

    @Override
    public List<WarehouseTransferDetail> getDetailsByTransferId(int transferId) {
        return detailRepository.findByTransferId(transferId);
    }

    public WarehouseTransferDetailRepository getDetailRepository() {
        return this.detailRepository;
    }

    @Override
    public List<WarehouseTransferDetail> getDetailsByWorkOrderId(int workOrderId) {
        return detailRepository.findByWorkOrderId(workOrderId);
    }
    @Override
    public WarehouseTransfer findExistingTransfer(int fromWarehouseId, int toWarehouseId, int workOrderId, String employeeId) {
        return detailRepository.findByFields(fromWarehouseId, toWarehouseId, workOrderId, employeeId)
                .orElse(null);
    }

    @Override
    public Map<String, Integer> getScannedQuantitiesByWO(int workOrderId) {
        return transferRepository.getScannedQuantitiesByWO(workOrderId);
    }

    @Override
    public Map<String, Integer> getActualReturnedByWorkOrderId(int workOrderId) {
        return transferRepository.getActualReturnedByWorkOrderId(workOrderId);
    }

    @Override
    public int getFromWarehouseIdByTransferId(int transferId) {
        return transferRepository.getFromWarehouseIdByTransferId(transferId);
    }


}

