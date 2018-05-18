package com.bozin.worldtraveler.Adapters;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bozin.worldtraveler.R;

import java.util.ArrayList;

/**
 * Created by igorb on 24.12.2017.
 */

public class InfoWindowRVAdapter extends RecyclerView.Adapter<InfoWindowRVAdapter.ImageViewHolder> {

    public final PictureOnClickHandler mPictureOnClickHandler;
    public final DeletePictureOnClickHandler mDeletePictureOnclickHandler;
    private int mDeletePictures;
    private Context mContext;
    private ArrayList<Uri> mPicturePaths;


    public InfoWindowRVAdapter(Context c, ArrayList<Uri> picturePaths, PictureOnClickHandler clickHandler, DeletePictureOnClickHandler deletePictureHandler, int deletePictures) {
        mPictureOnClickHandler = clickHandler;
        mContext = c;
        mPicturePaths = picturePaths;
        mDeletePictureOnclickHandler = deletePictureHandler;
        mDeletePictures = deletePictures;
    }

    public interface PictureOnClickHandler {
        void onPictureClick(Uri uri);
    }

    public interface DeletePictureOnClickHandler {
        void onDeletePictureClick(int position);
    }


    public void updatePictures(ArrayList<Uri> updatedPictures, int deletePictures){
        mPicturePaths = updatedPictures;
        mDeletePictures = deletePictures;
        if(updatedPictures != null){
            this.notifyDataSetChanged();
        }
    }
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InfoWindowRVAdapter.ImageViewHolder holder, int position) {

        if (mDeletePictures == 0) {
            holder.deletePicture.setVisibility(View.INVISIBLE);
        } else if (mDeletePictures == 1) {
            holder.deletePicture.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext).load(mPicturePaths.get(position)).into(holder.selectedPicture);
    }

    @Override
    public int getItemCount() {
        return mPicturePaths.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView selectedPicture;
        public Button deletePicture;


        public ImageViewHolder(View itemView) {
            super(itemView);
            selectedPicture = (ImageView) itemView.findViewById(R.id.rv_item_image);
            deletePicture = (Button) itemView.findViewById(R.id.btn_delete_picture);

            selectedPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();
                    Uri uri = mPicturePaths.get(position);
                    mPictureOnClickHandler.onPictureClick(uri);
                }
            });

            deletePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mDeletePictureOnclickHandler.onDeletePictureClick(position);
                }
            });
        }


    }


}
