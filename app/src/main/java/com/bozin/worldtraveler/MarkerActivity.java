package com.bozin.worldtraveler;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.motion.MotionLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.bozin.worldtraveler.Adapters.PictureRvAdapter;
import com.bozin.worldtraveler.data.AppDatabase;
import com.bozin.worldtraveler.data.AppExecutor;
import com.bozin.worldtraveler.data.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

/**
 * Created by igorb on 20.12.2017.
 */

public class MarkerActivity extends AppCompatActivity implements PictureRvAdapter.PictureOnClickHandler {


    private Button mBtnAddImage;
    private ArrayList<Uri> mPictureUris;
    private RecyclerView mPicturesRV;
    private PictureRvAdapter mAdapter;
    private int mCurrentMarkerID;
    public final static int PICK_PHOTO_CODE = 11;
    MarkerViewModel viewModel;
    private AppDatabase db;
    private MotionLayout motionLayout;

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent mIntent = getIntent();
        mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);
        mPictureUris = new ArrayList<>();

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

        mPicturesRV = findViewById(R.id.recyclerView);

        new AsyncTask<Integer, Void, Void>() {

            @Override
            protected Void doInBackground(Integer... integers) {
                db = AppDatabase.getInstance(MarkerActivity.this);
                MarkerViewModelFactory factory = new MarkerViewModelFactory(db, mCurrentMarkerID);
                viewModel = ViewModelProviders.of(MarkerActivity.this, factory).get(MarkerViewModel.class);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mPictureUris = getPicturePaths();

                if (mAdapter != null) {
                    mAdapter.updatePictures(mPictureUris);
                } else {
                    mAdapter = new PictureRvAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(MarkerActivity.this, LinearLayoutManager.VERTICAL, false);
                    DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
                    mPicturesRV.addItemDecoration(decoration);
                    mPicturesRV.setLayoutManager(layoutManager);
                    mPicturesRV.setAdapter(mAdapter);
                }

                motionLayout = findViewById(R.id.motion01_layout_activity_marker);
                motionLayout.transitionToEnd();

            }
        }.execute(mCurrentMarkerID);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // implement swipe to delete
                int position = viewHolder.getAdapterPosition();
                mPictureUris.remove(position);
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... integers) {
                        updatePicturePaths(mCurrentMarkerID, makePathString());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mAdapter.updatePictures(mPictureUris);
                    }
                }.execute(position);
            }
        }).attachToRecyclerView(mPicturesRV);

        mBtnAddImage = findViewById(R.id.btn_add_images);
        mBtnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {

            if (data.getData() != null) {
                Uri pictureUri = data.getData();
                getContentResolver().takePersistableUriPermission(pictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mPictureUris.add(pictureUri);
            } else {
                if (data.getClipData() != null) {


                    ClipData mClipData = data.getClipData();

                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mPictureUris.add(uri);
                    }
                }
            }


            mAdapter.updatePictures(mPictureUris);
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


    public class FetchImagesTask extends AsyncTask<ArrayList<Uri>, Void, PictureRvAdapter> {

        @Override
        protected PictureRvAdapter doInBackground(ArrayList<Uri>... picturePaths) {
            mAdapter = new PictureRvAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this);
            return mAdapter;
        }

        @Override
        protected void onPostExecute(PictureRvAdapter adapter) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(MarkerActivity.this, 2);
            DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
            mPicturesRV.addItemDecoration(decoration);
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


    @Override
    protected void onPause() {
        super.onPause();

    }


    public void delayedFinish() {
        super.finish();
    }

    @Override
    public void finish() {
        motionLayout.transitionToStart();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayedFinish();
            }
        }, 900);

    }
}

