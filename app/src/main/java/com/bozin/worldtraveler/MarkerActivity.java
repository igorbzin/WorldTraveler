package com.bozin.worldtraveler;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import com.bozin.worldtraveler.Adapters.PictureRvAdapter;
import com.bozin.worldtraveler.ViewModels.MarkerViewModel;
import com.bozin.worldtraveler.ViewModels.MarkerViewModelFactory;
import com.bozin.worldtraveler.model.RxObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

/**
 * Created by igorb on 20.12.2017.
 */

public class MarkerActivity extends AppCompatActivity implements PictureRvAdapter.PictureActionHandler {


    private final String TAG = "MarkerActivity";
    private ArrayList<Uri> mPictureUris;
    private RecyclerView mPicturesRV;
    private PictureRvAdapter mAdapter;
    public final static int PICK_PHOTO_CODE = 11;
    private MarkerViewModel viewModel;
    private RxObservableList.ObservableList<Uri> uriObservableList;

    private ConstraintLayout constraintLayout_start;
    private ConstraintSet constraintSetStart = new ConstraintSet();
    private ConstraintSet constraintSetEnd = new ConstraintSet();
    private ConstraintSet constraintSetSnackbar = new ConstraintSet();


    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        final Intent mIntent = getIntent();
        int mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);
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
        constraintLayout_start = findViewById(R.id.constraintlayout_marker);
        constraintSetStart.clone(constraintLayout_start);
        constraintSetEnd.clone(this, R.layout.motion01_end_marker_activity);
        constraintSetSnackbar.clone(this, R.layout.motion01_snackbar);

        //Initialize viewmodel
        MarkerViewModelFactory factory = new MarkerViewModelFactory(getApplication(), mCurrentMarkerID);
        viewModel = ViewModelProviders.of(MarkerActivity.this, factory).get(MarkerViewModel.class);

        uriObservableList = new RxObservableList.ObservableList<>();

        for(Uri uri: mPictureUris){
            uriObservableList.add(uri);
        }

        uriObservableList.getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Uri>() {
                    @Override
                    public void onNext(Uri uri) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        viewModel.getPlace()
                .map(this::getPicturePaths)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<ArrayList<Uri>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ArrayList<Uri> uris) {

                        mAdapter = new PictureRvAdapter(MarkerActivity.this, mPictureUris, MarkerActivity.this);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MarkerActivity.this, LinearLayoutManager.VERTICAL, false);
                        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
                        mPicturesRV.addItemDecoration(decoration);
                        mPicturesRV.setLayoutManager(layoutManager);
                        mPicturesRV.setAdapter(mAdapter);
                        beginTransition(constraintSetEnd, 850);
                        Log.d(TAG, "Successfully loaded pictures");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error loading pictures from database");

                    }
                });





        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public synchronized void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                beginTransition(constraintSetSnackbar, 300);
                mAdapter.onItemRemove(viewHolder, mPicturesRV);
            }


        }).attachToRecyclerView(mPicturesRV);

        Button mBtnAddImage = findViewById(R.id.btn_add_images);
        mBtnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
        });


    }


    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    //getContentResolver().takePersistableUriPermission(uri, flags);
                    mPictureUris.add(uri);

                }
            }

        }
    }


    private String makePathString(ArrayList<Uri> uriArrayList) {
        StringBuilder uriString = new StringBuilder();
        for (Uri uri : uriArrayList) {
            uriString.append(uri.toString()).append(",");
        }
        return uriString.toString();
    }

    @Override
    public void onPictureClick(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


    @Override
    public void onPictureSwipe(int duration) {
        beginTransition(constraintSetEnd, duration);
    }

    //Get Picture Uris from DB
    private ArrayList<Uri> getPicturePaths(Place place) {
        ArrayList<Uri> picturePathList = new ArrayList<>();
        String pictureUriString = place.getPicture_uris();
        if (pictureUriString != null && !pictureUriString.equals("")) {
            List<String> pathStringsArrayList = Arrays.asList(pictureUriString.split(","));
            for (int i = 0; i < pathStringsArrayList.size(); i++) {
                String path = pathStringsArrayList.get(i);
                picturePathList.add(Uri.parse(path));
            }
        }
        Log.d(TAG, "Picture uris successfully loaded");
        return picturePathList;
    }


    private void beginTransition(ConstraintSet constraintSet, int duration) {
        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration(duration);
        android.support.transition.TransitionManager.beginDelayedTransition(constraintLayout_start, autoTransition);
        constraintSet.applyTo(constraintLayout_start);
    }

    private void delayedFinish() {
        super.finish();
    }

    @Override
    public void finish() {
        beginTransition(constraintSetStart, 850);
        final Handler handler = new Handler();
        handler.postDelayed(this::delayedFinish, 900);
    }
}

