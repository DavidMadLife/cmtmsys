package org.chemtrovina.cmtmsys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.chemtrovina.cmtmsys.dto.EmployeeDto;
import org.chemtrovina.cmtmsys.model.Employee;
import org.chemtrovina.cmtmsys.model.enums.UserRole;
import org.chemtrovina.cmtmsys.security.RequiresRoles;
import org.chemtrovina.cmtmsys.service.base.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@RequiresRoles({
        UserRole.ADMIN,
        UserRole.EMPLOYEE,
        UserRole.EMPLOYEE_MINI
})

@Component
public class EmployeeUpdateDialogController {

    @FXML
    private TextField txtFullName;
    @FXML private TextField txtCompany;
    @FXML private TextField txtGender;
    @FXML private TextField txtPhone;
    @FXML private TextField txtJobTitle;
    @FXML private TextField txtManager;
    @FXML private TextArea txtNote;

    private Employee employee;

    @Autowired
    private EmployeeService employeeService;

    public void setEmployee(EmployeeDto dto) {
        this.employee = dto.toEntity(); // ⚠ entity đầy đủ

        txtFullName.setText(dto.getFullName());
        txtCompany.setText(dto.getCompany());
        txtGender.setText(dto.getGender());
        txtPhone.setText(dto.getPhoneNumber());
        txtJobTitle.setText(dto.getJobTitle());
        txtManager.setText(dto.getManagerName());
        txtNote.setText(dto.getNote());
    }

    @FXML
    private void onSave() {
        employee.setFullName(txtFullName.getText());
        employee.setCompany(txtCompany.getText());
        employee.setGender(txtGender.getText());
        employee.setPhoneNumber(txtPhone.getText());
        employee.setJobTitle(txtJobTitle.getText());
        employee.setManager(txtManager.getText());
        employee.setNote(txtNote.getText());

        employeeService.updateEmployee(employee);

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) txtFullName.getScene().getWindow()).close();
    }
}

