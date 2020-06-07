package com.neo.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.neo.whatsappclone.Chat.ChatObject;
import com.neo.whatsappclone.Chat.MediaAdapter;
import com.neo.whatsappclone.Chat.MessageAdapter;
import com.neo.whatsappclone.Chat.MessageObject;
import com.neo.whatsappclone.User.UserInfo;
import com.neo.whatsappclone.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Activity that handles users communication btw each other
 */
public class ChatActivity extends AppCompatActivity {
    public static final String CHAT_OBJECT = "CHAT_OBJECT";

    private ChatObject mChatObject;
    private RecyclerView mChat, mMedia;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;

    ArrayList<MessageObject> messageList;
    private DatabaseReference mChatMessagesDb;

    private static final int PICK_IMAGE_INTENT = 1;                                                   // request code for starting an activity for result
    private ArrayList<String> mediaUriList = new ArrayList<>();                                                           // list that holds the images choosen by the user
    private ArrayList<String> mediaIdList = new ArrayList<>();                                        // ArrayList for storing the media Id
    private int totalMediaUploaded = 0;                                                               // iterator that increases after each media is uploaded to the database
    private EditText mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatObject = (ChatObject) getIntent().getSerializableExtra(CHAT_OBJECT);                                             // gets the chat object from the intent used to start activity

        mChatMessagesDb = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Button mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        initMessageRecyclerView();
        initMediaRecyclerView();
        getChatMessages();
    }


    private void getChatMessages() {
        mChatMessagesDb.addChildEventListener(new ChildEventListener() {                // listener for children under this db reference
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String text = "",
                            creatorID = "";

                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if (dataSnapshot.child("text").getValue() != null) {
                        text = dataSnapshot.child("text").getValue().toString();
                    }
                    if (dataSnapshot.child("creator").getValue() != null) {
                        creatorID = dataSnapshot.child("creator").getValue().toString();
                    }
                    if(dataSnapshot.child("media").getChildrenCount() > 0){
                        // true if there's something inside this db ref, i.e the mediaUrls
                        for(DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren()){
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text, mediaUrlList);
                    messageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(messageList.size() - 1);                                    // makes it possible to auto scroll to last element or message added to the recy view
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        mMessage = findViewById(R.id.message);


        String messageId = mChatMessagesDb.push().getKey();

        final DatabaseReference newMessageDB = mChatMessagesDb.child(messageId);

        final Map newMessageMap = new HashMap<>();

        if (!mMessage.getText().toString().isEmpty()) {
            newMessageMap.put("text", mMessage.getText().toString());
        }
        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

        if (!mediaUriList.isEmpty()) {                                                                      // true when user wants to send an image
            for (String mediaUri : mediaUriList) {
                final String mediaId = newMessageDB.child("media").push().getKey();                               // returns a unique id for this specific media
                mediaIdList.add(mediaId);

                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child(messageId).child(mediaId);

                final UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));                              // uploads the media file to the storage
                // gets the download URl for use by other user when the media is sent to him
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {        // gets uri that can be used to get the media url
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());                // all done on the database storage
                                totalMediaUploaded++;
                                if (totalMediaUploaded == mediaUriList.size()) {
                                    // true only when all of the images in the storage ref has been uploaded successfully
                                    updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
                                }
                            }
                        });
                    }
                });
            }
        } else {
            if (!mMessage.getText().toString().isEmpty()) {
                // exec if the mediaList is empty
                updateDatabaseWithNewMessage(newMessageDB, newMessageMap);
            }

        }


    }


    /**
     * updates the DB with the media files Uri
     * also loop through users in the userList of the chatObject and find user not equal to sender Uid
     * and send messages and notification to them
     */
    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDB, Map newMessageMap) {
        newMessageDB.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        mMediaAdapter.notifyDataSetChanged();                                                       // clears up all images in the RecyView that displays the images user wants to send, since the list is cleared

        String message;

        if(newMessageMap.get("text") != null){
            // true only when a message as text is sent to a user
            message = newMessageMap.get("text").toString();
        } else{
            message = "Sent media";
        }

        for(UserInfo mUser : mChatObject.getUserList()){
            if(!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message, "New Message", mUser.getNotificationKey());
            }
        }
    }

    private void initMessageRecyclerView() {
        messageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);                                                 // enables the RecyclerView to scroll seamlessly
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(messageList);
        mChat.setAdapter(mChatAdapter);
    }


    private void initMediaRecyclerView() {
        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);                                                 // enables the RecyclerView to scroll seamlessly
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    /**
     * method create creates an intent that will call opening of gallery for choosing of images
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);                                     // tells the intent that user can choose multiple images
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (resultCode) {
                case PICK_IMAGE_INTENT:
                    if (data.getClipData() == null) {
                        // true only if user picks one image only
                        mediaUriList.add(data.getData().toString());
                    } else {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                        }
                    }
                    mMediaAdapter.notifyDataSetChanged();                                                // notify the recyView after media Url has been added to the list
                    break;
                default:
                    break;


            }
        }
    }
}
