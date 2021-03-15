package com.example.messanger.Repositories;

import com.example.messanger.Models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class DataBase {

    public FirebaseDatabase firebase;
    private long id;

    public DataBase() {
        this.firebase = FirebaseDatabase.getInstance();
    }

    public void save(User user) {
        DatabaseReference myRef = firebase.getReference().child("Users");
        myRef.child(String.valueOf(id)).setValue(user);
        increment();
    }


    public void increment() {
        id++;
    }

    public void setID(long id) {
        this.id = id;
    }


}
