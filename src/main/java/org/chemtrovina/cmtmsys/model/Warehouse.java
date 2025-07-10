package org.chemtrovina.cmtmsys.model;

public class Warehouse {
    private int warehouseId;
    private String name;

    public Warehouse(int warehouseId, String name) {
        this.warehouseId = warehouseId;
        this.name = name;
    }

    public Warehouse() {

    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
