package org.chemtrovina.cmtmsys.dto;

/** Item hiển thị trong ComboBox kho */
public class WarehouseOption {
    public final Integer id;       // có thể null với "Auto (current)"
    public final String name;
    public WarehouseOption(Integer id, String name) { this.id = id; this.name = name; }
    @Override public String toString() { return name; }
}
