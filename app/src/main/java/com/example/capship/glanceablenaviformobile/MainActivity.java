package com.example.capship.glanceablenaviformobile;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import jp.co.yahoo.android.maps.*;
import jp.co.yahoo.android.maps.routing.RouteOverlay;
import jp.co.yahoo.android.maps.navi.NaviController;

public class MainActivity extends AppCompatActivity implements RouteOverlay.RouteOverlayListener, NaviController.NaviControllerListener {

    private MapView mMapView = null;//MapViewメンバー
    private MyLocationOverlay _overlay;
    private String AppId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppId = "dj0zaiZpPTJUQTFGczJSSnRBViZzPWNvbnN1bWVyc2VjcmV0Jng9NDA-";

        mMapView = new MapView(this, AppId);

        //MyLocationOverlayインスタンス作成
        _overlay = new SubMyLocationOverlay(getApplicationContext(), mMapView, this);

        //現在位置取得開始
        _overlay.enableMyLocation();

        //MapViewにMyLocationOverlayを追加。
        mMapView.getOverlays().add(_overlay);
        mMapView.invalidate();

        setContentView(mMapView);
        //setContentView(R.layout.activity_main);
    }

    @Override
    protected void  onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Toast.makeText(this, "経路検索", Toast.LENGTH_SHORT).show();

            //RouteOverlay作成
            RouteOverlay routeOverlay = new RouteOverlay(this, AppId);

            //出発地ピンの吹き出し設定
            routeOverlay.setStartTitle("現在地点");

            //目的地ピンの吹き出し設定
            routeOverlay.setGoalTitle("広島県庁");

            //経由点ピンを非表示
            routeOverlay.setRoutePinVisible(false);

            //出発地、目的地、移動手段を設定
            routeOverlay.setRoutePos(_overlay.getMyLocation(), new GeoPoint(34396560, 132459622), RouteOverlay.TRAFFIC_WALK);

            //RouteOverlayListenerの設定
            routeOverlay.setRouteOverlayListener(this);

            //検索を開始
            routeOverlay.search();

            //MapViewにRouteOverlayを追加
            mMapView.getOverlays().add(routeOverlay);
            mMapView.invalidate();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean finishRouteSearch(RouteOverlay routeOverlay) {
        //NaviControllerを作成しRouteOverlayインスタンスを設定
        NaviController naviController = new NaviController(this,routeOverlay);

        //MapViewインスタンスを設定
        naviController.setMapView(mMapView);

        //NaviControllerListenerを設定
        naviController.setNaviControlListener(this);

        //案内処理を開始
        //naviController.start();   //←このメソッド呼び出しでハングする…

        return false;
    }

    @Override
    public boolean errorRouteSearch(RouteOverlay routeOverlay, int i) {
        return false;
    }

    @Override
    public boolean onLocationChanged(NaviController naviController) {
        //目的地までの残りの距離
        double rema_dist = naviController.getTotalDistance();

        //目的地までの残りの時間
        double rema_time = naviController.getTotalTime();

        //出発地から目的地までの距離
        double total_dist = naviController.getDistanceOfRemainder();

        //出発地から目的地までの時間
        double total_time = naviController.getTimeOfRemainder();

        //現在位置
        Location location = naviController.getLocation();
        return false;
    }

    @Override
    public boolean onLocationTimeOver(NaviController naviController) {
        return false;
    }

    @Override
    public boolean onLocationAccuracyBad(NaviController naviController) {
        return false;
    }

    @Override
    public boolean onRouteOut(NaviController naviController) {
        return false;
    }

    @Override
    public boolean onGoal(NaviController naviController) {
        //案内処理を継続しない場合は停止させる
        naviController.stop();
        return false;
    }
}
