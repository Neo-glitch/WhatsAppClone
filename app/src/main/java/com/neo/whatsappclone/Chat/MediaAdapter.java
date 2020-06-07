package com.neo.whatsappclone.Chat;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.neo.whatsappclone.R;

import java.util.ArrayList;

/**
 * work is to populate an imageView with the image Uri
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private ArrayList<String> mMediaUriList;
    private Context mContext;

    public MediaAdapter(Context context, ArrayList<String> mediaUriList){
        this.mContext = context;
        this.mMediaUriList = mediaUriList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        return new MediaViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Glide.with(mContext).
                load(Uri.parse(mMediaUriList.get(position))).
                into(holder.mMedia);
    }

    @Override
    public int getItemCount() {
//        return mMediaUriList.size() != 0 ? mMediaUriList.size() : 0;
        return mMediaUriList.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder{
        ImageView mMedia;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mMedia = itemView.findViewById(R.id.media);
        }
    }
}
