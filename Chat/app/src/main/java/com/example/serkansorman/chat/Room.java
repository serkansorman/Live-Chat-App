package com.example.serkansorman.chat;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Objects;

public class Room {

    private String name;
    private Integer numOfUser;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {

        return Objects.hash(name, numOfUser);
    }

    @Override
    public String toString() {
        return "Room{" +

                "name='" + name + '\'' +
                ", numOfUser=" + numOfUser +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumOfUser(int numOfUser) {
        this.numOfUser = numOfUser;
    }

    public Room(String name, int numOfUser) {
        this.name = name;

        this.numOfUser = numOfUser;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        Room room = (Room) o;
        return name.equals((room.getName()));

    }



    public Integer getNumOfUser() {

        return numOfUser;
    }



}
