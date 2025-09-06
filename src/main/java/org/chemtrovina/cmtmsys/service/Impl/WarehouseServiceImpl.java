package org.chemtrovina.cmtmsys.service.Impl;

import org.chemtrovina.cmtmsys.model.Warehouse;
import org.chemtrovina.cmtmsys.repository.base.WarehouseRepository;
import org.chemtrovina.cmtmsys.service.base.WarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;

    public WarehouseServiceImpl(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addWarehouse(Warehouse warehouse) {
        repository.add(warehouse);
    }

    @Override
    public void updateWarehouse(Warehouse warehouse) {
        repository.update(warehouse);
    }

    @Override
    public void deleteWarehouseById(int id) {
        repository.deleteById(id);
    }

    @Override
    public Warehouse getWarehouseById(int id) {
        return repository.findById(id);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return repository.findAll();
    }

    @Override
    public String getWarehouseNameByTransferId(int transferId, boolean isFrom) {
        return repository.getWarehouseNameByTransferId(transferId, isFrom);
    }

    @Override
    public Integer getIdByName(String name) {
        return repository.getIdByName(name);
    }

    @Override
    public Warehouse findByName(String name) {
        return repository.findByName(name);
    }


}
