package com.bozin.worldtraveler.Adapters;

import com.bozin.worldtraveler.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by igorb on 22.12.2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public CustomInfoWindowAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        @SuppressLint("InflateParams") View view = context.getLayoutInflater().inflate(R.layout.infowindow, null);
        TextView tvTitle = view.findViewById(R.id.tv_title_info);
        tvTitle.setText(marker.getTitle());
        return view;
    }
}