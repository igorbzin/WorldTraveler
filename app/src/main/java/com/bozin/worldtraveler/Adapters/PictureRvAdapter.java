package com.bozin.worldtraveler.Adapters;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bozin.worldtraveler.R;

import java.util.ArrayList;

/**
 * Created by igorb on 24.12.2017.
 */

public class PictureRvAdapter extends RecyclerView.Adapter<PictureRvAdapter.ImageViewHolder> {

    public final PictureOnClickHandler mPictureOnClickHandler;
    private Context mContext;
    private ArrayList<Uri> mPicturePaths;


    public PictureRvAdapter(Context c, ArrayList<Uri> picturePaths, PictureOnClickHandler clickHandler) {
        mPictureOnClickHandler = clickHandler;
        mContext = c;
        mPicturePaths = picturePaths;
    }

    public interface PictureOnClickHandler {
        void onPictureClick(Uri uri);
    }




    public void updatePictures(ArrayList<Uri> updatedPictures) {
        mPicturePaths = updatedPictures;
        if (updatedPictures != null) {
            this.notifyDataSetChanged();
        }
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PictureRvAdapter.ImageViewHolder holder, int position) {


        Glide.with(mContext).load(mPicturePaths.get(position)).into(holder.selectedPicture);
    }

    @Override
    public int getItemCount() {
        return mPicturePaths.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView selectedPicture;


        public ImageViewHolder(View itemView) {
            super(itemView);
            selectedPicture =  itemView.findViewById(R.id.rv_item_image);


            selectedPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Uri uri = mPicturePaths.get(position);
                    mPictureOnClickHandler.onPictureClick(uri);
                }
            });

        }


    }


}
