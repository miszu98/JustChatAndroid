package com.example.messanger.Models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Friends implements Serializable {
    private List<Integer> listFriends;

    public Friends() {
        this.listFriends = new ArrayList<>();
        this.listFriends.add(-1);
    }

    public List<Integer> getListFriends() {
        return listFriends;
    }

    public void setListFriends(List<Integer> listFriends) {
        this.listFriends = listFriends;
    }
}
