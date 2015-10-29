package com.example.capship.glanceablenaviformobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.MyLocationOverlay;

public class SubMyLocationOverlay extends MyLocationOverlay {

    MapView _mapView = null;
    Activity _activity = null;
    private ProgressDialog mProgDialog = null;//プログレスダイアログ

    private boolean mNaviFlag = false;

    public SubMyLocationOverlay(Context context, MapView mapView, Activity activity) {
        super(context, mapView);

        mNaviFlag = false;

        _mapView = mapView;
        _activity = activity;

        mProgDialog = new ProgressDialog(_activity);
        mProgDialog.setMessage("現在位置取得中");
        mProgDialog.setCancelable(false);
        mProgDialog.show();
    }

    //現在地更新のリスナーイベント
    @Override
    public void onLocationChanged(android.location.Location location) {
        super.onLocationChanged(location);

        if (mNaviFlag == true) {
            if (_mapView.getMapController() != null) {
                //位置が更新されると地図の位置も変える。
                GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
                _mapView.getMapController().animateTo(p);
                _mapView.invalidate();
            }
        }

        if(mProgDialog!=null){
            mProgDialog.dismiss();
            mProgDialog = null;
            if (_mapView.getMapController() != null) {
                //位置が更新されると地図の位置も変える。
                GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
                _mapView.getMapController().animateTo(p);
                _mapView.invalidate();
            }
        }
    }

    public void SetNaviFlag (boolean flag) {
        mNaviFlag = flag;
    }
}
