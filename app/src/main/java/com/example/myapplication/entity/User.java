package com.example.myapplication.entity;

import java.io.Serializable;

public class User{

    private int Id;

    private String name;
    // Getters and setters
    private String realpassword;
    private String phone;

    private int ischanged;
    public int getId() {
        return Id;
    }
    public int getIschanged() {
        return ischanged;
    }
    public void setIschanged(int ischanged) {
        this.ischanged = ischanged;
    }
    public void setId(int id) {
        Id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRealpassword() {
        return realpassword;
    }
    public void setRealpassword(String realpassword) {
        this.realpassword = realpassword;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }


}