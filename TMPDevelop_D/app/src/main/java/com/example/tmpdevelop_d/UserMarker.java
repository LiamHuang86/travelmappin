package com.example.tmpdevelop_d;

public class UserMarker {
    // 所有用戶圖釘的序號，從0開始
    int index;

    // 行程序號，默認未儲存到本地的臨時行程的 index 為 0，新增一個旅程（點擊儲存按鈕）後值從 1 開始計算
    int routeIndex;

    // 地點序號，為圖釘在對應 route 中的序號，第一個添加的圖釘序號為0
    // 新增一個旅程（點擊綠色添加圖釘按鈕）後值 +1，每當新增一個旅程（點擊儲存按鈕）後值重新從 0 開始計算
    int markerIndex;
    int pointCount;

    // 默認圖釘 = 0，其他顔色分別對應一個序號
    int markerIconIndex;

    double lat;
    double lng;

    UserMarker(int routeIndex, int markerIndex, int pointCount, int markerIconIndex, double lat, double lng){
        this.index = index;
        this.routeIndex = routeIndex;
        this.markerIndex = markerIndex;
        this.pointCount = pointCount;
        this.markerIconIndex = markerIconIndex;
        this.lat = lat;
        this.lng = lng;
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


    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
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

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getPointCount() {
        return pointCount;
    }


    // Index 為同一 routeIndex 下的圖釘編號
    public void setMarkerIconIndex(int markerIconIndex) {
        this.markerIconIndex = markerIconIndex;
    }

    public int getMarkerIconIndex() {
        return markerIconIndex;
    }


}
