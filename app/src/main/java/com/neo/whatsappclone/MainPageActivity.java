package com.neo.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.neo.whatsappclone.Chat.ChatListAdapter;
import com.neo.whatsappclone.Chat.ChatObject;
import com.neo.whatsappclone.User.UserInfo;
import com.onesignal.OneSignal;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    ArrayList<ChatObject> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        OneSignal.startInit(this).init();                                                // init OneSignal, to get device ready for pushNotification
        OneSignal.setSubscription(true);
        // gets a notificationKey that allows to us know to which user to send message to and store key in database
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        Fresco.initialize(this);                                                         // init Fresco before usage to prevent app from crashing when using lib

        Button mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneSignal.setSubscription(false);                                               // makes logout user not to recieve notification
                FirebaseAuth.getInstance().signOut();                                                                               // signs out the current user
                Intent intent = new Intent(MainPageActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                                                                     // kills every other activity started from this activity passed to intent since user logged out
                startActivity(intent);
                finish();

            }
        });

        Button mFindUser = findViewById(R.id.find_user);
        mFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
            }
        });

        getPermissions();
        initRecyclerView();
        getUserChatList();
    }

    /**
     * gets the chats from the Firebase DB
     */
    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").
                child(FirebaseAuth.getInstance().getUid()).child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {                                            // keeps listening for changes to this point in db
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {                             // loops though every single chat id that's in the user chat dir of the db
                        ChatObject mChat = new ChatObject(childSnapShot.getKey());                              // gets the unique chat id
                        boolean exists = false;

                        for (ChatObject mChatIterator : chatList) {
                            if (mChatIterator.getChatId().equals(mChat.getChatId())) {
                                exists = true;
                            }
                        }
                        if (exists) {
                            // true when the chatId is already found, so don't add to recyc view
                            continue;
                        }

                        chatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * gets the relevant chat info needed from the child 'chat' from the firebase database
     *
     * @param chatId
     */
    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String chatId = "";

                    if (dataSnapshot.child("id").getValue() != null) {
                        chatId = dataSnapshot.child("id").getValue().toString();                                        // gets the chatId agaian from the database since the  call is async
                        for (DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren()) {                    // loops through all users children in the child info snapshot
                            for (ChatObject mChatIterator : chatList) {                                                   // loop through chat list to find chat that has id == to the chatId in question
                                if (mChatIterator.getChatId().equals(chatId)) {
                                    UserInfo mUser = new UserInfo(userSnapshot.getKey());
                                    mChatIterator.addUserToUserList(mUser);                                               // stores the users associated with the given chat in the userList in the chatObject
                                    getUserData(mUser);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * gets the user info from users asso with this chat, especially users diff from sender
     * @param mUser
     */
    private void getUserData(UserInfo mUser) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid());
        mUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInfo mUser = new UserInfo(dataSnapshot.getKey());                                               // gets the id of the user

                if(dataSnapshot.child("notificationKey").getValue() != null){
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());
                }

                // loops through each chat to find chat with mUser in their UserArrayList
                for(ChatObject mChat : chatList){
                    for(UserInfo mUserIterator : mChat.getUserList()){
                        if(mUserIterator.getUid().equals(mUser.getUid())){
                            mUserIterator.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerView() {
        chatList = new ArrayList<>();
        mChatList = findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);                                                 // enables the RecyclerView to scroll seamlessly
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(chatList);
        mChatList.setAdapter(mChatListAdapter);
    }

    /**
     * needed to get realtime permissions to read from ar write to a phones contact list
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }


}
