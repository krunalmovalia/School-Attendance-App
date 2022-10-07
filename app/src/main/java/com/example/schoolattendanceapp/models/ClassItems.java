package com.example.schoolattendanceapp.models;

public class ClassItems {
    private long cid;

    public ClassItems(long cid, String className, String division) {
        this.cid = cid;
        this.className = className;
        this.division = division;
    }

    private String className;
    private String division;

    public ClassItems(String className, String division) {
        this.className = className;
        this.division = division;
    }
    //CONSTRUCTOR OF GET CLASS
    public String getClassName() {
        return className;
    }
    //CONSTRUCTOR OF SET CLASS
    public void setClassName(String className) {
        this.className = className;
    }
    //CONSTRUCTOR OF GET DIVISION
    public String getDivision() {
        return division;
    }
    //CONSTRUCTOR OF SET DIVISION
    public void setDivision(String division) {
        this.division = division;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }
}
