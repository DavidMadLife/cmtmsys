package org.chemtrovina.cmtmsys.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class History {
    private int id;
    private Integer invoiceId;
    private LocalDate date;
    private LocalTime time;
    private String maker;
    private String makerPN;
    private String sapPN;
    private int quantity;
    private String employeeId;
    private String status;
    private String scanCode;
    private String MSL;
    private String invoicePN;
    private String spec;

    //When get into View
    private String invoiceNo;

    public History() {
    }

    public History(int id, Integer invoiceId, LocalDate date, LocalTime time,
                   String maker, String makerPN, String sapPN, int quantity,
                   String employeeId, String status, String scanCode, String MSL, String invoicePN, String spec) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.date = date;
        this.time = time;
        this.maker = maker;
        this.makerPN = makerPN;
        this.sapPN = sapPN;
        this.quantity = quantity;
        this.employeeId = employeeId;
        this.status = status;
        this.scanCode = scanCode;
        this.MSL = MSL;
        this.invoicePN = invoicePN;
        this.spec = spec;

    }


    public String getSpec(){
        return spec;
    }
    public void setSpec(String spec){
        this.spec = spec;
    }

    public String getInvoicePN(){
        return invoicePN;
    }
    public void setInvoicePN(String invoicePN){
        this.invoicePN = invoicePN;
    }


    //Using for View
    public String getInvoiceNo() {
        return invoiceNo;
    }
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }


    // Getter và Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getMakerPN() {
        return makerPN;
    }

    public void setMakerPN(String makerPN) {
        this.makerPN = makerPN;
    }

    public String getSapPN() {
        return sapPN;
    }

    public void setSapPN(String sapPN) {
        this.sapPN = sapPN;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScanCode() {
        return scanCode;
    }

    public void setScanCode(String scanCode) {
        this.scanCode = scanCode;
    }

    public String getMSL() {
        return MSL;
    }
    public void setMSL(String MSL) {
        this.MSL = MSL;
    }


    @Override
    public String toString() {
        return date + " " + time + " - " + makerPN + " (" + quantity + ") - " + status;
    }
}
