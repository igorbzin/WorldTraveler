package com.example.styledmap;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by igorb on 20.12.2017.
 */

public class MarkerActivity extends AppCompatActivity {


    private Button mBtnAddImage;
    private ArrayList<Uri> mArrayUri;
    private ArrayList<Bitmap> mBitmapsSelected;
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
        try {
            if (data.getClipData() != null) {

                ClipData mClipData = null;
                mClipData = data.getClipData();

                mArrayUri = new ArrayList<>();
                mBitmapsSelected = new ArrayList<>();
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    mArrayUri.add(uri);

                    Uri photoUri = data.getData();
                    // !! You may need to resize the image if it's too large
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mBitmapsSelected.add(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
