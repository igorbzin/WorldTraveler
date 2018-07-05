package com.bozin.worldtraveler.Adapters;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bozin.worldtraveler.MarkerActivity;
import com.bumptech.glide.Glide;
import com.bozin.worldtraveler.R;

import java.util.ArrayList;

/**
 * Created by igorb on 24.12.2017.
 */

public class PictureRvAdapter extends RecyclerView.Adapter<PictureRvAdapter.ImageViewHolder> {

    private final PictureOnClickHandler mPictureOnClickHandler;
    private Context mContext;
    private ArrayList<Uri> mPicturePaths;
    final private Object LOCK = new Object();


    public PictureRvAdapter(Context c, ArrayList<Uri> picturePaths, PictureOnClickHandler clickHandler) {
        mPictureOnClickHandler = clickHandler;
        mContext = c;
        mPicturePaths = picturePaths;
    }

    public interface PictureOnClickHandler {
        void onPictureClick(Uri uri);
    }


    public void onItemRemove(final RecyclerView.ViewHolder viewHolder, final RecyclerView recyclerView, ConstraintSet set) {
        synchronized (LOCK){
            final int adapterPosition = viewHolder.getAdapterPosition();
            final Uri mUri = mPicturePaths.get(adapterPosition);
            final ConstraintSet constraintSet = set;
            Snackbar snackbar = Snackbar
                    .make(recyclerView , R.string.sb_deleted_image, Snackbar.LENGTH_LONG)
                    .setAction("UNDO", view -> {
                        mPicturePaths.add(adapterPosition, mUri);
                        notifyItemInserted(adapterPosition);
                        recyclerView.smoothScrollToPosition(adapterPosition);
                        MarkerActivity.beginTransition(constraintSet,300);
                    });

            snackbar.addCallback(new Snackbar.Callback(){

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    if(event== Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_ACTION){
                        MarkerActivity.beginTransition(constraintSet,300);
                    }
                }
            });
            View snackbar_view = snackbar.getView();
            TextView action_snackbar = snackbar_view.findViewById(android.support.design.R.id.snackbar_action);
            action_snackbar.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
            snackbar_view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            snackbar.show();
            mPicturePaths.remove(adapterPosition);
            notifyItemRemoved(adapterPosition);
        }

    }

    public void updatePictures(ArrayList<Uri> updatedPictures) {
        mPicturePaths = updatedPictures;
        if (updatedPictures != null) {
            this.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureRvAdapter.ImageViewHolder holder, int position) {
        Glide.with(mContext).load(mPicturePaths.get(position)).into(holder.selectedPicture);
    }

    @Override
    public int getItemCount() {
        return mPicturePaths.size();
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {

         ImageView selectedPicture;


         ImageViewHolder(View itemView) {
            super(itemView);
            selectedPicture =  itemView.findViewById(R.id.rv_item_image);


            selectedPicture.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Uri uri = mPicturePaths.get(position);
                mPictureOnClickHandler.onPictureClick(uri);
            });

        }


    }


}
