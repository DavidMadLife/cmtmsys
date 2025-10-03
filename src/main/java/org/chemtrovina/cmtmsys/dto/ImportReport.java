package org.chemtrovina.cmtmsys.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportReport {
    public int totalRows;         // tổng số dòng đọc được (không tính header & dòng trống)
    public int inserted;          // số bản ghi đã chèn
    public int skippedExisting;   // số bản ghi bỏ qua vì đã tồn tại trong DB (trùng Code)
    public int skippedDuplicate;  // trùng Code ngay trong file
    public int skippedInvalid;    // dữ liệu không hợp lệ (thiếu Code, sai ngày, ...)
    public final List<String> errors = new ArrayList<>(); // chi tiết lỗi từng dòng

    @Override public String toString() {
        return "Tổng: " + totalRows +
                "\nĐã thêm: " + inserted +
                "\nBỏ qua (đã có trong DB): " + skippedExisting +
                "\nBỏ qua (trùng trong file): " + skippedDuplicate +
                "\nBỏ qua (dữ liệu lỗi): " + skippedInvalid +
                (errors.isEmpty() ? "" : "\n\nChi tiết lỗi:\n - " + String.join("\n - ", errors));
    }
}
