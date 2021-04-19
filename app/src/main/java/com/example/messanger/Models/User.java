package com.example.messanger.Models;


import java.io.Serializable;

public class User implements Serializable {

    private String login;
    private String password;
    private String email;
    private String name;
    private String surName;
    private int age;
    private int phoneNumber;
    private String profilePhotoID;
    private Friends friends;
    private Invites invites;
    private String country;

    public User() {
    }

    public User(String country, String login, String password, String email, String name, String surName, int age, int phoneNumber, String profilePhotoID) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surName = surName;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.profilePhotoID = profilePhotoID;
        this.friends = new Friends();
        this.invites = new Invites();
        this.country = country;
    }

    public Friends getFriends() {
        return friends;
    }

    public void setFriends(Friends friends) {
        this.friends = friends;
    }

    public Invites getInvites() {
        return invites;
    }

    public void setInvites(Invites invites) {
        this.invites = invites;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProfilePhotoID() {
        return profilePhotoID;
    }

    public void setProfilePhotoID(String profilePhotoID) {
        this.profilePhotoID = profilePhotoID;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", surName='" + surName + '\'' +
                ", age=" + age +
                ", phoneNumber=" + phoneNumber +
                ", profilePhotoID='" + profilePhotoID + '\'' +
                '}';
    }

}
