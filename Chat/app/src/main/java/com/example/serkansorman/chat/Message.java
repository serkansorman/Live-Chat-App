package com.example.serkansorman.chat;

/*
 * Created by Serkan Sorman on 01.07.2018.
 */

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Message {

    private String text;
    private String userName;
    private String time;
    private String id;

    public Message() {
    }

    public Message(String text, String userName,String id) {
        this.text = text;
        this.userName = userName;
        this.id = id;
        setTime();
    }


    private void setTime() {
        Calendar time;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sp = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        time = Calendar.getInstance();
        this.time = sp.format(time.getTime());
    }


    public String getText() {return text; }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        if (userName != null ? !userName.equals(message.userName) : message.userName != null)
            return false;
        if (time != null ? !time.equals(message.time) : message.time != null) return false;
        return id != null ? id.equals(message.id) : message.id == null;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }


}
