package com.dessert.mojito;

import android.app.Activity;
import android.os.Bundle;
import com.amap.api.maps.*;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;


/**
 * Created by Lawrence on 20/11/2016.
 *
 */

public class MonitorActivity extends Activity {

    MapView mMapView = null;

    private AMap aMap;
    private UiSettings mUiSettings;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        init();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();

        }
        latLng = new LatLng(39, 116);
        final Marker marker = aMap.addMarker(new MarkerOptions().
                position(latLng).
                title("用户当前位置").
                snippet("DefaultMarker"));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomPosition(0);
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
    }

}
