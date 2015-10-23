package com.example.capship.glanceablenaviformobile;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;

import jp.co.yahoo.android.maps.GeoPoint;
import jp.co.yahoo.android.maps.MapView;
import jp.co.yahoo.android.maps.ar.ARController;
import jp.co.yahoo.android.maps.navi.CompassOverlay;
import jp.co.yahoo.android.maps.routing.GPoint;
import jp.co.yahoo.android.maps.routing.LocationControl;
import jp.co.yahoo.android.maps.routing.RLine;
import jp.co.yahoo.android.maps.routing.RouteControl;
import jp.co.yahoo.android.maps.routing.RouteOverlay;
import jp.co.yahoo.android.maps.viewlayer.Coordinate;
import jp.co.yahoo.android.maps.viewlayer.LLCalculation;

public class CustomNaviController implements LocationControl.LocationControlListener {
    private RouteOverlay m_routeOverlay = null;
    private Context m_context = null;
    private MapView m_mapView = null;
    private ARController m_arController = null;
    private CustomNaviController.NaviControllerListener m_naviControlListener = null;
    private LocationControl m_locationControl = null;
    private RouteControl m_routeControl = null;
    private Location m_nowLocation = null;
    private boolean m_goalFlag = false;
    private int m_routeOutCnt = 0;
    private long m_startGpsTime = 0L;
    private double m_oldRemainderDistance = 0.0D;
    private double m_startRemainderDistance = 0.0D;
    private long m_speed = 0L;
    private boolean m_navi_go = false;
    private CompassOverlay m_compassOrverlay = null;
    private static int GOAL_DIST_M = 10;
    private static int ROUTE_DIST_M = 30;
    private static int ROUTEOUT_CNT = 30;
    private static int NAVI_DEBG = 0;
    private int m_pincnt = 0;
    private Drawable m_arkeiyuImage = null;

    public CustomNaviController(Context context, RouteOverlay routeOverlay) {
        this.m_context = context;
        this.m_routeOverlay = routeOverlay;
        this.m_routeControl = (RouteControl)this.m_routeOverlay.getObject();
    }

    public void setMapView(MapView mapView) {
        if(mapView == null && this.m_mapView != null && this.m_compassOrverlay != null) {
            this.m_compassOrverlay.stopCompass();
            this.m_mapView.getOverlays().remove(this.m_compassOrverlay);
        }

        this.m_mapView = mapView;
    }

    public void setNaviControlListener(CustomNaviController.NaviControllerListener naviControlListener) {
        this.m_naviControlListener = naviControlListener;
    }

    public double getTotalDistance() {
        return this.m_routeControl.getMTotalDistance();
    }

    public double getTotalTime() {
        return (double)this.m_routeControl.defTotalTime;
    }

    public boolean getNaviEnabled() {
        return this.m_locationControl != null;
    }

    public Location getLocation() {
        return this.m_nowLocation;
    }

    public boolean start() {
        if(this.m_locationControl != null) {
            return false;
        } else {
            this.m_routeOutCnt = 0;
            this.m_locationControl = new LocationControl(this.m_context, this);
            if(NAVI_DEBG == 1) {
                this.m_locationControl.setDebgData(this.m_routeControl.getCoordinates());
            }

            this.m_locationControl.setTimer(120000);
            this.m_locationControl.startLocation(true);
            this.m_navi_go = true;
            if(this.m_mapView != null) {
                this.m_compassOrverlay = new CompassOverlay(this.m_context, this.m_mapView);
                this.m_mapView.getOverlays().add(this.m_compassOrverlay);
                if(this.m_routeOverlay != null) {
                    GeoPoint gp = this.m_routeOverlay.getStartPos();
                    if(gp != null) {
                        this.m_compassOrverlay.setpos(gp, 0);
                    }
                }
            }

            return true;
        }
    }

    public boolean stop() {
        if(this.m_locationControl != null) {
            this.m_locationControl.stopLocation();
            this.m_locationControl = null;
        }

        if(this.m_mapView != null) {
            this.m_mapView.getOverlays().remove(this.m_compassOrverlay);
        }

        if(this.m_compassOrverlay != null) {
            this.m_compassOrverlay.stopCompass();
            this.m_compassOrverlay = null;
        }

        this.m_startGpsTime = 0L;
        return true;
    }

    public double getDistanceOfRemainder() {
        if(this.m_routeControl == null) {
            return 0.0D;
        } else if(this.m_nowLocation == null) {
            return this.getTotalDistance();
        } else if(!this.m_navi_go) {
            return this.getTotalDistance();
        } else if(this.m_goalFlag) {
            return 0.0D;
        } else {
            GPoint gp = new GPoint(this.m_nowLocation.getLongitude(), this.m_nowLocation.getLatitude());
            RLine wkguide = this.m_routeControl.getNowRLine();
            if(wkguide == null) {
                return this.getTotalDistance();
            } else {
                double wkdist = this.m_routeControl.getMDistanceNearPointToPoint(gp, wkguide.getLastPoint());
                wkdist += this.m_routeControl.getMTotalDistanceByLNo(wkguide.lno + 1);
                return wkdist;
            }
        }
    }

    public double getTimeOfRemainder() {
        double dist = this.getDistanceOfRemainder();
        long wkl = 0L;
        if(this.m_speed == 0L) {
            return (double)this.m_routeControl.defTotalTime;
        } else {
            wkl = (long)(dist / (double)this.m_speed + 0.5D);
            return this.m_goalFlag?0.0D:(wkl == 0L?1.0D:(double)wkl);
        }
    }

    private Coordinate getNowPointByCoordinate() {
        if(this.m_routeControl != null && this.m_nowLocation != null) {
            GPoint gp = new GPoint(this.m_nowLocation.getLongitude(), this.m_nowLocation.getLatitude());
            GPoint nowp = this.m_routeControl.getNearPoint(gp);
            Coordinate cd = new Coordinate();
            if(nowp == null) {
                return null;
            } else {
                cd.lat = nowp.y;
                cd.lon = nowp.x;
                return cd;
            }
        } else {
            return null;
        }
    }

    private GPoint getGuidePoint() {
        if(this.m_routeControl != null && this.m_nowLocation != null) {
            RLine line = this.m_routeControl.getNowRLine();

            for(int i = line.lno; i < this.m_routeControl.count(); ++i) {
                RLine next_line = this.m_routeControl.getRLine(i);
                if(next_line.direction != null) {
                    int wkflag = (int)Long.parseLong(next_line.direction);
                    if(wkflag != 0 && wkflag != 1 && wkflag != 11) {
                        return next_line.getLastPoint();
                    }
                }
            }

            return line.getLastPoint();
        } else {
            return null;
        }
    }

    public int getNextDirection() {
        if(this.m_routeControl != null && this.m_nowLocation != null) {
            int flag = 0;
            RLine line = this.m_routeControl.getNextRoute();
            int dist = (int)line.distance;

            for(int i = line.lno; i < this.m_routeControl.count(); ++i) {
                RLine next_line = this.m_routeControl.getRLine(i);
                if(next_line.direction != null) {
                    int wkflag = (int)Long.parseLong(next_line.direction);
                    if(wkflag != 0 && wkflag != 1 && wkflag != 11) {
                        flag = wkflag;
                        break;
                    }

                    dist += (int)next_line.distance;
                }
            }

            return flag;
        } else {
            return 0;
        }
    }

    public double getDistanceToNextDirection() {
        if(this.m_routeControl == null) {
            return 0.0D;
        } else {
            RLine line = this.m_routeControl.getNowRLine();
            int dist = (int)line.distance;

            for(int i = line.lno + 1; i < this.m_routeControl.count(); ++i) {
                RLine next_line = this.m_routeControl.getRLine(i);
                if(next_line.direction != null) {
                    int wkflag = (int)Long.parseLong(next_line.direction);
                    if(wkflag != 0 && wkflag != 1 && wkflag != 11) {
                        break;
                    }

                    dist += (int)next_line.distance;
                }
            }

            return (double)dist;
        }
    }

    public void onYLocationChanged(LocationControl location) {
        if(this.m_routeControl != null) {
            this.m_nowLocation = location.getLocation();
            GPoint gp = new GPoint(this.m_nowLocation.getLongitude(), this.m_nowLocation.getLatitude());
            this.m_routeControl.cmpLineAndPoint(gp);
            double line_dist = this.m_routeControl.getMDistanceToNearPoint(gp);
            if(this.m_naviControlListener != null) {
                Coordinate wkcd = gp.getCoordinate();
                Coordinate nowpoint = this.getNowPointByCoordinate();
                GPoint lastpoint = this.m_routeControl.getLastPoint();
                Coordinate lastcd = new Coordinate();
                lastcd.lat = lastpoint.y;
                lastcd.lon = lastpoint.x;
                double wkcd_distance = LLCalculation.distance(wkcd.lat, wkcd.lon, lastcd.lat, lastcd.lon);
                double nowpoint_distance = LLCalculation.distance(nowpoint.lat, nowpoint.lon, lastcd.lat, lastcd.lon);
                if(line_dist >= (double)ROUTE_DIST_M) {
                    float var14 = this.m_nowLocation.getAccuracy();
                    if(var14 > (float)ROUTE_DIST_M) {
                        this.m_naviControlListener.onLocationAccuracyBad(this);
                        if(this.m_compassOrverlay != null) {
                            this.m_compassOrverlay.setpos(new GeoPoint((int)(this.m_nowLocation.getLatitude() * 1000000.0D), (int)(this.m_nowLocation.getLongitude() * 1000000.0D)), (int)var14);
                        }

                        return;
                    }

                    if(this.m_routeOutCnt > ROUTEOUT_CNT) {
                        this.m_routeOutCnt = 0;
                        this.m_naviControlListener.onRouteOut(this);
                    } else {
                        this.checkSpeed();
                        this.m_naviControlListener.onLocationChanged(this);
                        ++this.m_routeOutCnt;
                    }
                } else if(wkcd_distance > (double)GOAL_DIST_M && nowpoint_distance > (double)GOAL_DIST_M) {
                    this.checkSpeed();
                    this.m_routeOutCnt = 0;
                    if(this.m_arController != null) {
                        this.m_arController.setCurrentPos(wkcd.getLatitude(), wkcd.getLongitude(), 0.0D, 0.0F);
                        if(this.getNextDirection() == 10) {
                            if(this.m_pincnt == 1) {
                                this.m_arController.removePOI(1);
                            }

                            this.m_arController.setDestination(0);
                            this.m_pincnt = 0;
                        } else {
                            if(this.m_pincnt == 1) {
                                this.m_arController.removePOI(1);
                            }

                            GPoint wkgp = this.getGuidePoint();
                            this.m_arController.addPOI(wkgp.y, wkgp.x, this.m_arkeiyuImage, 41, 38);
                            this.m_arController.setDestination(1);
                            this.m_pincnt = 1;
                        }
                    }

                    this.m_naviControlListener.onLocationChanged(this);
                } else {
                    location.setLocationControlListener((LocationControl.LocationControlListener)null);
                    location.stopLocation();
                    location = null;
                    if(this.m_arController != null) {
                        this.m_arController.setArrowColor(0.3F, 1.0F, 0.0F, 0.0F);
                    }

                    this.m_naviControlListener.onLocationChanged(this);
                    this.m_goalFlag = true;
                    this.m_naviControlListener.onGoal(this);
                }
            }

            if(this.m_compassOrverlay != null) {
                this.m_compassOrverlay.setpos(new GeoPoint((int)(this.m_nowLocation.getLatitude() * 1000000.0D), (int)(this.m_nowLocation.getLongitude() * 1000000.0D)));
            }
        }

    }

    public void onYLocationError(LocationControl location) {
        this.m_naviControlListener.onLocationTimeOver(this);
    }

    public void checkSpeed() {
        if(this.m_startGpsTime == 0L) {
            this.m_startGpsTime = System.currentTimeMillis();
            this.m_startRemainderDistance = this.getDistanceOfRemainder();
            this.m_oldRemainderDistance = this.m_startRemainderDistance;
        } else {
            long newTime = System.currentTimeMillis();
            double newRemainderDistance = this.getDistanceOfRemainder();
            double wkDistance = this.m_oldRemainderDistance - newRemainderDistance;
            if(wkDistance > 10.0D) {
                wkDistance = this.m_startRemainderDistance - newRemainderDistance;
                double wkSpeed = wkDistance / (double)(newTime - this.m_startGpsTime);
                this.m_speed = (long)(wkSpeed * 60000.0D);
                this.m_oldRemainderDistance = newRemainderDistance;
            }

        }
    }

    public interface NaviControllerListener {
        boolean onLocationChanged(CustomNaviController var1);

        boolean onLocationTimeOver(CustomNaviController var1);

        boolean onLocationAccuracyBad(CustomNaviController var1);

        boolean onRouteOut(CustomNaviController var1);

        boolean onGoal(CustomNaviController var1);
    }
}
