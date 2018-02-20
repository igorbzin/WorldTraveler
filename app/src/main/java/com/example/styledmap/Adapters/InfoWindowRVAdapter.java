package com.example.styledmap.Adapters;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.styledmap.R;

import java.util.ArrayList;

/**
 * Created by igorb on 24.12.2017.
 */

public class InfoWindowRVAdapter extends RecyclerView.Adapter<InfoWindowRVAdapter.ImageViewHolder> {

    public final InfoWindowRVAdapterOnClickHandler mClickHandler;
    private Context mContext;
    private ArrayList<Uri> mPicturePaths;


    public InfoWindowRVAdapter(Context c, ArrayList<Uri> picturePaths, InfoWindowRVAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
        mContext = c;
        mPicturePaths= picturePaths;
    }

    public interface InfoWindowRVAdapterOnClickHandler{
        void onClick(Uri uri);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InfoWindowRVAdapter.ImageViewHolder holder, int position) {
        Glide.with(mContext).load(mPicturePaths.get(position)).into(holder.selectedPicture);
    }

    @Override
    public int getItemCount() {
        return mPicturePaths.size();
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView selectedPicture;


        public ImageViewHolder(View itemView){
            super(itemView);
            selectedPicture = (ImageView) itemView.findViewById(R.id.rv_item_image);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Uri uri = mPicturePaths.get(position);
            mClickHandler.onClick(uri);
        }
    }


}
