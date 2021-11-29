package com.example.myoungjimohaji;

public class MessageItem {

    String stdNo;
    String message;
    String time;

    public MessageItem(String stdNo, String message, String time) {
        this.stdNo = stdNo;
        this.message = message;
        this.time = time;
    }

    //firebase DB에 객체로 값을 읽어올 때..
    //파라미터가 비어있는 생성자가 핑요함.
    public MessageItem() {
    }

    //Getter & Setter
    public String getStdNo() {
        return stdNo;
    }

    public void setName(String stdNo) {
        this.stdNo = stdNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
