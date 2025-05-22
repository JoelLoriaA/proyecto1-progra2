package com.MagicalStay.shared.domain;

public class FrontDeskClerk {
    private String name;
    private String lastNames;
    private String employeeId;
    private int phoneNumber;
    private String user;
    private String password;

    public FrontDeskClerk(String name, String lastNames, String employeeId, int phoneNumber, String user, String password) {
        this.name = name;
        this.lastNames = lastNames;
        this.employeeId = employeeId;
        this.phoneNumber = phoneNumber;
        this.user = user;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastNames() {
        return lastNames;
    }

    public void setLastNames(String lastNames) {
        this.lastNames = lastNames;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "FrontDeskClerk{" +
                "name='" + name + '\'' +
                ", lastNames='" + lastNames + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
