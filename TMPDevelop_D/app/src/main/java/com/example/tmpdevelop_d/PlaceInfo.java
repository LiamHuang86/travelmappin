package com.example.tmpdevelop_d;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlaceInfo {
    public static Boolean isComplete; // 消費資訊是否完整
    public static String costInfoKey; // 從雲端查找到的 CostInfo 的索引值
    public static LatLng location; // 坐標 LatLng(double Lat, double Lng)
    public static int routeIndex; // 在一條默認行程中所對應的路徑的 Index
    public static int markerIndex; // 在一條路徑中所對應的圖釘的 Index
    public static int iconIndex; // 圖釘的 Icon 序號
    public static String placeName; // 地點名稱
    public static String date; // 消費日期
    public static int hour; // 消費時間對應的小時
    public static int minute; // 消費時間對應的分鐘
    public static GroupInfo groupInfo; // 本地使用，不存到 FireBase
    public static List<UserInfo> friendInfoList = new ArrayList<UserInfo>(); // 朋友清單
    public static List<UserInfo> participantInfoList = new ArrayList<UserInfo>(); // 參與者清單
    public static String itemName; // 消費項目名稱
    public static UserInfo payer; // 付款的使用者
    public static int expense; // 消費金額
}
