package com.neo.whatsappclone.Chat;


import com.neo.whatsappclone.User.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * class is used to contain the chatId and the Title
 */
public class ChatObject implements Serializable {
    private static final long serialVersionUID = 1;
    private String mChatId;

    private ArrayList<UserInfo> mUserList = new ArrayList<>();                                              // list that holds the AppUsers info associated with a chat

    public ChatObject(String chatId) {
        this.mChatId = chatId;
    }

    public String getChatId() {
        return mChatId;
    }

    public ArrayList<UserInfo> getUserList() {
        return mUserList;
    }

    /**
     * Adds user to the UserList
     *
     * @param user
     */
    public void addUserToUserList(UserInfo user) {
        mUserList.add(user);
    }

}
