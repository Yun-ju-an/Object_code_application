package com.example.myoungjimohaji;

public class UserHelperClass {

    String stdNo, name, pNo, id, pw;

    public UserHelperClass() { }

    public UserHelperClass(String stdNo, String name, String pNo, String id, String pw) {
        this.stdNo = stdNo;
        this.name = name;
        this.pNo = pNo;
        this.id = id;
        this.pw = pw;
    }

    public String getStdNo() {
        return stdNo;
    }

    public String getName() {
        return name;
    }

    public String getpNo() {
        return pNo;
    }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }
}
