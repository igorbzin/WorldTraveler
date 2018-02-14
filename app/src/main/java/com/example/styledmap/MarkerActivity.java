package com.example.styledmap;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class MarkerActivity extends AppCompatActivity {


    private Button mBtnAddImage;
    private ArrayList<Uri> mPictureUris;
    private RecyclerView mPicturesRV;
    private InfoWindowRVAdapter mAdapter;
    private String mUriString = "";
    private int mCurrentMarkerID;
    public final static int PICK_PHOTO_CODE = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent mIntent = getIntent();
        mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);

        //Inflate layout, set display metrics
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.75f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        getWindow().setLayout((int) (width * .9), (int) (height * .6));


        mPictureUris = new ArrayList<>();

        mPicturesRV = (RecyclerView) findViewById(R.id.recyclerView);
        mPictureUris = MapsActivity.getPicturePaths(mCurrentMarkerID);

        if(mPictureUris != null){
            InfoWindowRVAdapter adapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris);
            GridLayoutManager layoutManager = new GridLayoutManager(MarkerActivity.this, 2);
            mPicturesRV.setHasFixedSize(true);
            mPicturesRV.setLayoutManager(layoutManager);
            mPicturesRV.setAdapter(adapter);
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

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data.getClipData() != null) {

            ClipData mClipData = data.getClipData();

            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();
                mPictureUris.add(uri);
            }
            new FetchImagesTask().execute(mPictureUris);

            makePathString();
            MapsActivity.updatePicturePaths(mCurrentMarkerID, mUriString);
        }

    }


    private void makePathString(){
        for (int i = 0; i < mPictureUris.size(); i++){
            mUriString += mPictureUris.get(i).toString() + "," ;
        }
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }



    public class FetchImagesTask extends AsyncTask<ArrayList<Uri>, Void, InfoWindowRVAdapter> {

        @Override
        protected InfoWindowRVAdapter doInBackground(ArrayList<Uri>... picturePaths) {
            mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mPictureUris);
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

