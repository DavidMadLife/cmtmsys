package org.chemtrovina.cmtmsys.model.enums;

public enum UserRole {
    ADMIN,
    INVENTORY, //Role này thì chỉ được quản lý kho
    EMPLOYEE, //ROle này chỉ được quản lý employee
    SUBLEEDER, //Role này chỉ vào được lập kế hoạch
    GENERALWAREHOUSE,
}
