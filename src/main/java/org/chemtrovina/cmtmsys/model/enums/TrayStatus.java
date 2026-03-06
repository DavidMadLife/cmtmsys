package org.chemtrovina.cmtmsys.model.enums;

public enum TrayStatus {

    IN_STOCK,          // đang trong kho SMT
    ISSUED_TO_ROM,     // đã xuất đi ROM table
    IN_ROM,            // đang ở ROM table
    RETURNED_FROM_ROM, // đã nạp xong trả về kho
    IN_CHAMBER,        // trong tủ chamber
    ISSUED_TO_LINE,    // xuất cho line sản xuất
    CONSUMED,          // đã dùng hết
    SPLIT_PARENT,      // tray cha sau khi split (ẩn)
    VOID,               // huỷ
    PROGRAMMED,
    NEW
}
