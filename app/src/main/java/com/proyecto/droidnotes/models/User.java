package com.proyecto.droidnotes.models;

public class User {

    private String id;
    private String username;
    private String phone;
    private String image;
    private String info;
    private String token;
    private String email;
    private String account;
    private String career;
    private boolean isSelected;

    public User() {

    }

    public User(String id, String username, String phone, String image, String info, String token, String email, String account, String career, boolean isSelected) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.image = image;
        this.info = info;
        this.token = token;
        this.email = email;
        this.account = account;
        this.career = career;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
