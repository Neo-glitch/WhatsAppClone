package com.neo.whatsappclone.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.neo.whatsappclone.R;

import java.util.ArrayList;
import java.util.HashMap;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyViewHolder> {

    private ArrayList<UserInfo> userList;

    public UserListAdapter(ArrayList<UserInfo> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListAdapter.MyViewHolder holder, int position) {
        holder.mName.setText(userList.get(position).getUserName());
        holder.mPhone.setText(userList.get(position).getPhoneNumber());

        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // sets the selected var to true whenever the checkbox is clicked
                userList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });

//        holder.mLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                createChat(holder.getAdapterPosition());
//            }
//        });
    }


    @Override
    public int getItemCount() {
        if (userList.size() != 0) {
            return userList.size();
        } else {
            return 0;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mPhone;
        CheckBox mAdd;
        LinearLayout mLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mPhone = itemView.findViewById(R.id.phone);
            mAdd = itemView.findViewById(R.id.add);
            mLayout = itemView.findViewById(R.id.layout);
        }
    }
}
