package com.neo.whatsappclone.Chat;

import java.util.ArrayList;

public class MessageObject {
    private String mMessageId,
            mSenderId,
            mMessage;

    private ArrayList<String> mediaUrlList;                                                         // list is used to get and store the url of the media saved in the database

    public MessageObject(String messageId, String senderId, String message, ArrayList<String> mediaUrlList) {
        this.mMessageId = messageId;
        this.mSenderId = senderId;
        this.mMessage = message;
        this.mediaUrlList = mediaUrlList;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }

    public void setMediaUrlList(ArrayList<String> mediaUrlList) {
        this.mediaUrlList = mediaUrlList;
    }
}
