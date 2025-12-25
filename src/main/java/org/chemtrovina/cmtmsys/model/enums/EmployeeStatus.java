package org.chemtrovina.cmtmsys.model.enums;

public enum EmployeeStatus {
    Null(0,"Null"),
    ACTIVE(1, "Đang làm"),
    INACTIVE(2, "Đã nghỉ"),
    ON_LEAVE(3, "Nghỉ phép");

    private final int code;
    private final String label;

    EmployeeStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static EmployeeStatus fromCode(int code) {
        for (EmployeeStatus status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }

    public static EmployeeStatus fromLabel(String label) {
        if (label == null) return null;

        for (EmployeeStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid EmployeeStatus label: " + label);
    }

}
