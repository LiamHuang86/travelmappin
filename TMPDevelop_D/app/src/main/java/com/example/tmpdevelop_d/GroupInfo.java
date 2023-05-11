package com.example.tmpdevelop_d;

import java.util.ArrayList;
import java.util.List;

public class GroupInfo {
    String groupName;
    String groupId;
    String avatar;
    List<UserInfo> friendInfoList = new ArrayList<UserInfo>();
    boolean isSelected;

    public GroupInfo(String groupName, List<UserInfo> friendInfoList, String avatar) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.avatar = avatar;
        this.friendInfoList = friendInfoList;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setFriendInfoList(List<UserInfo> friendInfoList) {
        this.friendInfoList = friendInfoList;
    }

    public List<UserInfo> getFriendInfoList() {
        return friendInfoList;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected(){
        return isSelected;
    }
}
