package org.chemtrovina.cmtmsys.dto;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.ShiftPlanEmployee;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftPlanRow {

    private final int employeeId;
    private final String MSCNID1;
    private final String MSCNID2;
    private final String fullName;
    private String managerName;

    // ✅ Dùng BooleanProperty để binding được với TableView
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final Map<LocalDate, String> shiftMap = new HashMap<>();

    public ShiftPlanRow(Employee emp, List<LocalDate> dates, List<ShiftPlanEmployee> plans) {
        this.employeeId = emp.getEmployeeId();
        this.MSCNID1 = emp.getMSCNID1();
        this.MSCNID2 = emp.getMSCNID2();
        this.fullName = emp.getFullName();
        this.managerName = emp.getManager();   // 🔥 map vào đây (emp.getManager() là String)

        // init ngày
        for (LocalDate d : dates) {
            shiftMap.put(d, "");
        }

        if (plans != null) {
            for (ShiftPlanEmployee p : plans) {
                shiftMap.put(p.getShiftDate(), p.getShiftCode());
            }
        }
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }


    public int getEmployeeId() {
        return employeeId;
    }

    public String getMSCNID1() {
        return MSCNID1;
    }

    public String getMSCNID2() {
        return MSCNID2;
    }

    public String getFullName() {
        return fullName;
    }

    // ================= SHIFT ====================
    public String getShiftForDate(LocalDate date) {
        return shiftMap.getOrDefault(date, "");
    }

    public void setShiftForDate(LocalDate date, String shiftCode) {
        shiftMap.put(date, shiftCode);
    }

    // ================ SELECTED ==================
    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
