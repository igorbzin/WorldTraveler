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

public class MarkerActivity extends AppCompatActivity {


    private Button mBtnAddImage;
    private ArrayList<Uri> mArrayUri;
    private RecyclerView mPicturesRV;
    private InfoWindowRVAdapter mAdapter;
    private String mUriString = "";
    public final static int PICK_PHOTO_CODE = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {

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


        mPicturesRV = (RecyclerView) findViewById(R.id.recyclerView);
        mArrayUri = new ArrayList<>();
        mArrayUri = MapsActivity.getPictureUris(MapsActivity.currentMarkerID);
        if(mArrayUri != null){
            InfoWindowRVAdapter adapter = new InfoWindowRVAdapter(MarkerActivity.this, mArrayUri);
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
                mArrayUri.add(uri);
            }
            new FetchImagesTask().execute(mArrayUri);

            makeUriString();
            MapsActivity.updatePictureUris(MapsActivity.currentMarkerID, mUriString);
        }

    }

    private void makeUriString(){
        for (int i = 0; i < mArrayUri.size(); i++){
            mUriString += mArrayUri.get(i).toString() + "," ;
        }
    }

    public class FetchImagesTask extends AsyncTask<ArrayList<Uri>, Void, InfoWindowRVAdapter> {

        @Override
        protected InfoWindowRVAdapter doInBackground(ArrayList<Uri>... pictureUris) {
            mAdapter = new InfoWindowRVAdapter(MarkerActivity.this, mArrayUri);
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

