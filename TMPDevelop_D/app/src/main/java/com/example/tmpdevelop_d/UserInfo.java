package com.example.tmpdevelop_d;

public class UserInfo {
    String userName;
    String userId;
    String avatar;
    boolean isSelected;

    public UserInfo(String userName, String userId, String avatar) {
        this.userName = userName;
        this.userId = userId;
        this.avatar = avatar;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }

    public boolean isSelected(){
        return isSelected;
    }
}
