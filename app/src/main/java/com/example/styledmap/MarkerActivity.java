package com.example.styledmap;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.styledmap.Adapters.InfoWindowRVAdapter;

import java.util.ArrayList;

/**
 * Created by igorb on 20.12.2017.
 */

public class MarkerActivity extends AppCompatActivity implements InfoWindowRVAdapter.PictureOnClickHandler, InfoWindowRVAdapter.DeletePictureOnClickHandler {


    private Button mBtnAddImage;
    private ArrayList<Uri> mPictureUris;
    private RecyclerView mPicturesRV;
    private InfoWindowRVAdapter mAdapter;
    private int mCurrentMarkerID;
    private int mDeletingPictures; // 0 = not deleting, 1 deleting button is pressed
    public final static int PICK_PHOTO_CODE = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent mIntent = getIntent();
        mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);

        //Inflate layout, set display metrics
        super.onCreate(savedInstanceState);

        mPictureUris = new ArrayList<>();
        mPictureUris = MapsActivity.getPicturePaths(mCurrentMarkerID);

        setContentView(R.layout.activity_marker);

        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.75f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .6));

        mDeletingPictures = 0;

        mPicturesRV = (RecyclerView) findViewById(R.id.recyclerView);

        if (mPictureUris != null) {
            mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this, MarkerActivity.this, mDeletingPictures);
            GridLayoutManager layoutManager = new GridLayoutManager(MarkerActivity.this, 2);
            mPicturesRV.setHasFixedSize(true);
            mPicturesRV.setLayoutManager(layoutManager);
            mPicturesRV.setAdapter(mAdapter);
        }


        mBtnAddImage = (Button) findViewById(R.id.btn_add_images);
        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
            }
        });

        Button btnDeleteImage = (Button) findViewById(R.id.btn_delete_images);
        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeletingPictures = 1 - mDeletingPictures;
                mPicturesRV.invalidate();
                mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this, MarkerActivity.this, mDeletingPictures);
                mPicturesRV.setAdapter(mAdapter);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {

            if (data.getClipData() != null) {

                ClipData mClipData = data.getClipData();

                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    mPictureUris.add(uri);
                }

            } else if (data.getData() != null) {
                Uri pictureUri = data.getData();
                mPictureUris.add(pictureUri);
            }
            new FetchImagesTask().execute(mPictureUris);
            MapsActivity.updatePicturePaths(mCurrentMarkerID, makePathString());
        }

    }


    private String makePathString() {
        String uriString = "";
        for (int i = 0; i < mPictureUris.size(); i++) {
            uriString += mPictureUris.get(i).toString() + ",";
        }
        return uriString;
    }

    @Override
    public void onPictureClick(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    @Override
    public void onDeletePictureClick(int position) {
        mPictureUris.remove(position);
        MapsActivity.updatePicturePaths(mCurrentMarkerID, makePathString());
        mAdapter.notifyDataSetChanged();
    }


    public class FetchImagesTask extends AsyncTask<ArrayList<Uri>, Void, InfoWindowRVAdapter> {

        @Override
        protected InfoWindowRVAdapter doInBackground(ArrayList<Uri>... picturePaths) {
            mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this, MarkerActivity.this, mDeletingPictures);
            return mAdapter;
        }

        @Override
        protected void onPostExecute(InfoWindowRVAdapter adapter) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(MarkerActivity.this, 2);
            mPicturesRV.setLayoutManager(gridLayoutManager);
            mPicturesRV.setAdapter(mAdapter);
        }
    }


}

