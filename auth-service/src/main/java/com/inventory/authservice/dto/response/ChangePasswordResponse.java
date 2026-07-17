package com.inventory.authservice.dto.response;

public class ChangePasswordResponse {


    private String employeeCode;

    private String message;



    public ChangePasswordResponse() {
    }



    public String getEmployeeCode() {
        return employeeCode;
    }


    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }



    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }
}