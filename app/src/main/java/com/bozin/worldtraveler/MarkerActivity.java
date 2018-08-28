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

import com.bozin.worldtraveler.adapters.PictureRvAdapter;
import com.bozin.worldtraveler.model.RxObservableList;
import com.bozin.worldtraveler.viewModels.MarkerViewModel;
import com.bozin.worldtraveler.viewModels.MarkerViewModelFactory;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

/**
 * Created by igorb on 20.12.2017.
 */

public class MarkerActivity extends AppCompatActivity implements PictureRvAdapter.PictureActionHandler {

    private final String TAG = "MarkerActivity";
    private RecyclerView mPicturesRV;
    private PictureRvAdapter mAdapter;
    public final static int PICK_PHOTO_CODE = 11;
    private MarkerViewModel viewModel;
    private RxObservableList<Uri> uriObservableList;

    private ConstraintLayout constraintLayout_start;
    private ConstraintSet constraintSetStart = new ConstraintSet();
    private ConstraintSet constraintSetEnd = new ConstraintSet();
    private ConstraintSet constraintSetSnackbar = new ConstraintSet();


    @SuppressLint({"StaticFieldLeak", "CheckResult"})
    @Override
    public void onCreate(Bundle savedInstanceState) {

        final Intent mIntent = getIntent();
        int mCurrentMarkerID = mIntent.getIntExtra("MarkerID", 0);

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


        //Init Adapter
        mAdapter = new PictureRvAdapter(MarkerActivity.this, new ArrayList<>(), MarkerActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MarkerActivity.this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mPicturesRV.addItemDecoration(decoration);
        mPicturesRV.setLayoutManager(layoutManager);
        mPicturesRV.setAdapter(mAdapter);


        viewModel.getPlace()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(place -> viewModel.getPicturePaths(place))
                .toObservable()
                .concatMap((Function<ArrayList<Uri>, Observable<RxObservableList<Uri>>>) uriArrayList -> {

                    mAdapter.updatePictures(uriArrayList);
                    beginTransition(constraintSetEnd, 850);

                    Log.d(TAG, "Adapter pictures updated");
                    uriObservableList = new RxObservableList<>(uriArrayList);

                    return uriObservableList.getObservable()
                            .observeOn(Schedulers.io())
                            .doOnNext(uris -> {
                                Log.d(TAG, "Current List size: " + uris.getList().size());
                                String pathString = viewModel.makePathString(uris.getList());
                                viewModel.updatePicturePaths(pathString);
                                Log.d(TAG, "Updated pictures in database");
                            });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RxObservableList<Uri>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "Onsubscribe called");
                    }

                    @Override
                    public void onNext(RxObservableList<Uri> uris) {
                        mAdapter.updatePictures(uris.getList());
                        Log.d(TAG, "Adapter pictures updated, list size: " + uriObservableList.getList().size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "OnError : " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "OnComplete called");
                    }
                });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder
                    viewHolder, RecyclerView.ViewHolder target) {
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
        mBtnAddImage.setOnClickListener(v ->

        {
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
                    uriObservableList.add(uri);
                    Log.d(TAG, "Uri added to uriObservableList: " + uri.toString());
                }
            }

        }
    }




    @Override
    public void onPictureClick(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }


    @Override
    public void onPictureSwipe(int duration, ArrayList<Uri> uris) {
        uriObservableList.setList(uris);
        beginTransition(constraintSetEnd, duration);
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

