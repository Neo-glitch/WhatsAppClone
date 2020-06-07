package com.neo.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.neo.whatsappclone.User.UserInfo;
import com.neo.whatsappclone.User.UserListAdapter;
import com.neo.whatsappclone.Utils.CountryToPhonePrefix;

import java.util.ArrayList;
import java.util.HashMap;

public class FindUserActivity extends AppCompatActivity {

    private RecyclerView mUserList;
    private RecyclerView.Adapter mUserListAdapter;
    private RecyclerView.LayoutManager mUserListLayoutManager;
    private ArrayList<UserInfo> userList, mContactList;                                         // userList is list for all users of our app(shown on recy view), and contactList is list of our contacts


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);

        mContactList = new ArrayList<>();
        userList = new ArrayList<>();

        Button btnCreate = findViewById(R.id.create);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });
        initRecyclerView();
        getContactList();
    }


    /**
     * creates a chat child in the database of the user that was checked
     */
    private void createChat() {
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();                               // returns a unique id that's not in the "chat" path i.e chatId
        DatabaseReference chatInfoDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        // simply just updates the user node part of the db
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");


        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);


        boolean validChat = false;                                                                  // true only when a user is checked to avoid creating a chatRoom with no user
        // loops through the userList in the recyclerView to see which one is selected or not and add it to chatRoom in db
        for (UserInfo user : userList) {
            if (user.isSelected()) {
                validChat = true;
                newChatMap.put("users/" + user.getUid(), true);
                // for the user that we clicked on
                userDB.child(user.getUid()).child("chat").child(key).setValue(true);
            }
        }

        if (validChat) {
            chatInfoDB.updateChildren(newChatMap);
            userDB.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        }
    }


    private void getContactList() {
        String ISOPrefix = getCountryISO();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null);
        while (phones.moveToNext()) {
            // true as long as cursor is able to move to next pos
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // normalizes or reformats the phone numbers
            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            phoneNumber = phoneNumber.replace("(", "");
            phoneNumber = phoneNumber.replace(")", "");

            if (!String.valueOf(phoneNumber.charAt(0)).equals("+")) {
                phoneNumber = ISOPrefix + phoneNumber;
            }

            UserInfo mContact = new UserInfo("", name, phoneNumber);
            mContactList.add(mContact);

            getUserDetails(mContact);
        }
    }


    /**
     * gets user details from the online database if it's there
     *
     * @param mContact
     */
    private void getUserDetails(UserInfo mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "",
                            name = "";
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = childSnapshot.child("phone").getValue().toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = childSnapshot.child("name").getValue().toString();
                        }
                        UserInfo mUser = new UserInfo(childSnapshot.getKey(), name, phone);

                        if (name.equals(phone)) {
                            // true if user still has the default name, which is a phone number and rename to exact matching contact name
                            for (UserInfo mContactIterator : mContactList) {
                                if (mContactIterator.getPhoneNumber().equals(mUser.getPhoneNumber())) {
                                    mUser.setUserName(mContactIterator.getUserName());
                                }
                            }
                        }
                        userList.add(mUser);
                        // tells adapter that something has been added to the list
                        mUserListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * fetches the countryISO of the phone that is being used and assoc with every contacts on phone without iso
     * i.e ENG, BE
     *
     * @return the country code associated with the iso
     */
    private String getCountryISO() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (!telephonyManager.getNetworkCountryIso().equals("")) {                                // true when iso of the phone is not empty
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return CountryToPhonePrefix.getPhone(iso);
    }


    private void initRecyclerView() {
        mUserList = findViewById(R.id.userList);
        mUserList.setNestedScrollingEnabled(false);                                                 // enables the RecyclerView to scroll seamlessly
        mUserList.setHasFixedSize(false);
        mUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mUserList.setLayoutManager(mUserListLayoutManager);
        mUserListAdapter = new UserListAdapter(userList);
        mUserList.setAdapter(mUserListAdapter);
    }

}
