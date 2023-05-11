package com.example.tmpdevelop_d;

import com.google.android.gms.maps.model.LatLng;

public class UserPolyline {
    // 行程序號，默認未儲存到本地的臨時行程的index為0，新增一個旅程（點擊藍色儲存按鈕）後值從1開始計算
    int routeIndex;

    // 地點序號，為圖釘在對應route中的序號，第一個添加的圖釘序號為0
    // 新增一個旅程（點擊綠色添加圖釘按鈕）後值+1，每當新增一個旅程（點擊藍色儲存按鈕）後值重新從0開始計算
    int markerIndex;

    // 端點數量（指定行程的圖釘總數量）
    int pointCount;

    double lat;
    double lng;

    LatLng latLng;

    UserPolyline(int routeIndex, int pointCount, LatLng latLng){
        this.routeIndex = routeIndex;
        this.markerIndex = markerIndex;
        this.pointCount = pointCount;
        this.lat = lat;
        this.lng = lng;
        this.latLng = latLng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLng() {
        return lng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    // 若某幾個圖釘的 routeIndex 數值相同, 則為同一條折線
    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public int increaseRouteIndex() {
        routeIndex++;
        return routeIndex;
    }

    // Index 為同一 routeIndex 下的圖釘編號
    public void setMarkerIndex(int markerIndex) {
        this.markerIndex = markerIndex;
    }

    public int getMarkerIndex() {
        return markerIndex;
    }

    // 端點數量（指定行程的圖釘總數量）
    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getPointCount() {
        return pointCount;
    }
}
