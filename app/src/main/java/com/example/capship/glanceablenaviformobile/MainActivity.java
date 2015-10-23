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

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements RouteOverlay.RouteOverlayListener, CustomNaviController.NaviControllerListener {

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
            //routeOverlay.setGoalTitle("広島県庁");
            routeOverlay.setGoalTitle("熊平製作所");

            //経由点ピンを非表示
            routeOverlay.setRoutePinVisible(true);

            //出発地、目的地、移動手段を設定
            //routeOverlay.setRoutePos(_overlay.getMyLocation(), new GeoPoint(34396560, 132459622), RouteOverlay.TRAFFIC_WALK);
            routeOverlay.setRoutePos(_overlay.getMyLocation(), new GeoPoint(34365286, 132471866), RouteOverlay.TRAFFIC_WALK);

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
        CustomNaviController naviController = new CustomNaviController(this,routeOverlay);

        //MapViewインスタンスを設定
        naviController.setMapView(mMapView);

        //NaviControllerListenerを設定
        naviController.setNaviControlListener(this);

        //案内処理を開始
        naviController.start();

        return false;
    }

    @Override
    public boolean errorRouteSearch(RouteOverlay routeOverlay, int i) {
        return false;
    }

    @Override
    public boolean onLocationChanged(CustomNaviController naviController) {

        double next_dist = 0;
        int next_dire = 0;

        //目的地までの残りの距離
        //double rema_dist = naviController.getTotalDistance();

        //目的地までの残りの時間
        //double rema_time = naviController.getTotalTime();

        //出発地から目的地までの距離
        //double total_dist = naviController.getDistanceOfRemainder();

        //出発地から目的地までの時間
        //double total_time = naviController.getTimeOfRemainder();

        //次の経由地点情報の取得
        //next_dist = naviController.getDistanceOfRemainder();
        next_dist = naviController.getDistanceToNextDirectionOfRemainder();
        next_dire = naviController.getNextDirection();

        //現在位置
        Location location = naviController.getLocation();

        Toast.makeText(this, format("%s,%s", next_dist, convertDirection(next_dire)), Toast.LENGTH_SHORT).show();

        return false;
    }
    private String convertDirection(int dirValue){

        String direction = "?";

        switch (dirValue){
            case 0:
                direction = "直進";
                break;
            case 1:
                direction = "直進";
                break;
            case 2:
                direction = "右折";
                break;
            case 3:
                direction = "左折";
                break;
            case 4:
                direction = "斜め前方右方向";
                break;
            case 5:
                direction = "斜め前方左方向";
                break;
            case 6:
                direction = "斜め後方右方向";
                break;
            case 7:
                direction = "斜め後方左方向";
                break;
            case 9:
                direction = "出発地";
                break;
            case 10:
                direction = "目的地";
                break;
            case 12:
                direction = "横断歩道を渡る";
                break;
            case 13:
                direction = "道路を渡る";
                break;
            case 14:
                direction = "歩道橋を渡る";
                break;
            case 15:
                direction = "踏切を渡る";
                break;
            case 16:
                direction = "連絡通路へ進む";
                break;
            case 17:
                direction = "屋内通路へ進む";
                break;
            case 18:
                direction = "敷地内通路へ進む";
                break;
            case 19:
                direction = "歩道へ進む";
                break;
        }

        return direction;
    }

    @Override
    public boolean onLocationTimeOver(CustomNaviController naviController) {
        return false;
    }

    @Override
    public boolean onLocationAccuracyBad(CustomNaviController naviController) {
        return false;
    }

    @Override
    public boolean onRouteOut(CustomNaviController naviController) {
        return false;
    }

    @Override
    public boolean onGoal(CustomNaviController naviController) {
        //案内処理を継続しない場合は停止させる
        naviController.stop();
        return false;
    }
}
