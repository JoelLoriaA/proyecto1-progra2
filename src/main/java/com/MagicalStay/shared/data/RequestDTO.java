package com.MagicalStay.shared.data;

public class RequestDTO {
    private String operation;
    private Integer id;
    private String data;

    public RequestDTO(String operation, Integer id, String data) {
        this.operation = operation;
        this.id = id;
        this.data = data;
    }

    // Getters y setters
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}