package com.bozin.worldtraveler;

import android.arch.lifecycle.ViewModelProviders;
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

import com.bozin.worldtraveler.Adapters.InfoWindowRVAdapter;
import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.AppExecutor;
import com.bozin.worldtraveler.data.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    MarkerViewModel viewModel;
    private AppDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent mIntent = getIntent();
        mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);

        //Inflate layout, set display metrics
        super.onCreate(savedInstanceState);

        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db = AppDatabase.getInstance(MarkerActivity.this);
                MarkerViewModelFactory factory = new MarkerViewModelFactory(db, mCurrentMarkerID);
                viewModel = ViewModelProviders.of(MarkerActivity.this, factory).get(MarkerViewModel.class);
                mPictureUris = getPicturePaths();
            }
        });

        mPictureUris = new ArrayList<>();



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

        mPicturesRV = findViewById(R.id.recyclerView);

        if (mPictureUris != null) {
            mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this, MarkerActivity.this, mDeletingPictures);
            GridLayoutManager layoutManager = new GridLayoutManager(MarkerActivity.this, 2);
            mPicturesRV.setHasFixedSize(true);
            mPicturesRV.setLayoutManager(layoutManager);
            mPicturesRV.setAdapter(mAdapter);
        }


        mBtnAddImage = findViewById(R.id.btn_add_images);
        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
            }
        });

        Button btnDeleteImage = findViewById(R.id.btn_delete_images);
        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeletingPictures = 1 - mDeletingPictures;
                mAdapter.updatePictures(mPictureUris, mDeletingPictures);
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
            mAdapter.updatePictures(mPictureUris, mDeletingPictures);
            AppExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    updatePicturePaths(mCurrentMarkerID, makePathString());
                }
            });

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
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                updatePicturePaths(mCurrentMarkerID, makePathString());

            }
        });
        mAdapter.updatePictures(mPictureUris, mDeletingPictures);
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


    //Get Picture Uris from DB
    private ArrayList<Uri> getPicturePaths() {
        Place place = viewModel.getPlace();
        ArrayList<Uri> picturePathList = new ArrayList<>();
        String pictureUriString = place.getPicture_uris();
        if (pictureUriString != null) {
            List<String> pathStringsArrayList = Arrays.asList(pictureUriString.split(","));
            for (int i = 0; i < pathStringsArrayList.size(); i++) {
                String path = pathStringsArrayList.get(i);
                picturePathList.add(Uri.parse(path));
            }
        }
        return picturePathList;
    }


    //Update picture URIS to make deleting of pictures possible
    public void updatePicturePaths(int id, String picturePaths) {
        viewModel.updatePicturePaths(picturePaths);
    }

}

