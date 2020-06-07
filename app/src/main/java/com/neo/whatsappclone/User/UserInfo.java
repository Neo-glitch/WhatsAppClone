package com.neo.whatsappclone.User;

import java.io.Serializable;

/**
 * class fields are used to store the user info
 * Uid field is unique to each user
 */
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1;

    private String mUid, mUserName, mPhoneNumber, mNotificationKey;

    private boolean selected = false;                                                               // lets us know if an item(user) is selected or not

    public UserInfo(String uid) {
        this.mUid = uid;
    }

    public UserInfo(String Uid, String userName, String phoneNumber) {
        this.mUid = Uid;
        this.mUserName = userName;
        this.mPhoneNumber = phoneNumber;
    }

    public String getUid() {
        return mUid;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public String getNotificationKey() {
        return mNotificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        mNotificationKey = notificationKey;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
