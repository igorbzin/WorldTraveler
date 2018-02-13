package com.example.styledmap.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.styledmap.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by igorb on 24.12.2017.
 */

public class InfoWindowRVAdapter extends RecyclerView.Adapter<InfoWindowRVAdapter.ImageViewHolder> {

    private Context mContext;
    private ArrayList<Uri> mPictureUris;


    public InfoWindowRVAdapter(Context c, ArrayList<Uri> pictureLinks){
        mContext = c;
        mPictureUris = pictureLinks;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item,parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InfoWindowRVAdapter.ImageViewHolder holder, int position) {
        Picasso.with(mContext).load(mPictureUris.get(position)).fit().into(holder.selectedPicture);
    }

    @Override
    public int getItemCount() {
        return mPictureUris.size();
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView selectedPicture;

        public ImageViewHolder(View itemView){
            super(itemView);
            selectedPicture = (ImageView) itemView.findViewById(R.id.rv_item_image);
        }


    }


}
