package com.example.tmpdevelop_d;


import static android.app.Activity.RESULT_OK;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    public List<UserMarker> markerList = new ArrayList<>(); // 從FireBase獲取到的圖釘資訊
    public List<UserMarker> tempMarkerList = new ArrayList<>();
    public List<UserMarker> savedMarkerList = new ArrayList<>();
    public List<UserPolyline> polylineList = new ArrayList<>();
    public List<LatLng> latLngList = new ArrayList<>();
    public Map<Integer,UserMarker> marker = new HashMap();
    public NavController navController;
    public NavHostFragment navHostFragment;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    double lat = 0; // 當前所在的緯度（畫面中心）
    double lng = 0; // 當前所在的經度（畫面中心）
    int routeCount = 0; // 初始化行程序號，0代表暫未被儲存的行程
    int markerCount = 0; // 初始化單次行程内的圖釘序號
    //private FragmentMapBinding binding;
    private GoogleMap mMap;
    private MapView mapView;
    private View mView;
    private ImageView mLocationImage;
    private ActionBar actionBar; // 使用 AndroidX
    //boolean circleMenuButtonExpanded = false;
    //int addMarkerCount = 0; // 點擊按鈕添加圖釘的次數，更換新行程後重新從0開始計算

    // 兩個LatLng地點之間構成的方形區域，限制搜索的範圍僅覆蓋台灣
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(21.715956, 119.419628),new LatLng(25.371160, 122.138744));

    private EditText mEditText;
    private FusedLocationProviderClient client;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("CostInfo");
    Boolean getTargetCostInfo = false;
    int pointCount = 0;


    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("地圖");
        }

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) mView.findViewById(R.id.map);
        if (mapView != null) {
            // Initialise the MapView
            mapView.onCreate(null);
            mapView.onResume();
            // Set the map ready callback to receive the GoogleMap object
            mapView.getMapAsync(this);
        }
        //navController = navHostFragment.getNavController();
        //navController = Navigation.findNavController(view);
        //navController = Navigation.findNavController(requireActivity(), R.id.nav_host);

        mLocationImage = (ImageView) mView.findViewById(R.id.icon_location);
        mEditText = (EditText) mView.findViewById(R.id.search_edit_text);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //大部分可對地圖進行的的操作
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MapsInitializer.initialize(getContext());

        // 地圖初始化時隱藏已被創建的弧形按鈕菜單
        CircleMenuView mainCircleMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        mainCircleMenu.setVisibility(View.INVISIBLE);

        // 地圖初始化時隱藏已被創建的弧形按鈕菜單
        CircleMenuView subCircleMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_sub);
        subCircleMenu.setVisibility(View.INVISIBLE);

        // 調用實例化地點搜索功能的方法
        initSearchBar();

        // 調用實例化移動到當前所在地點按鈕的方法
        initLocationButton();

        // 調用實例化圓形展開按鈕菜單（主菜單）的方法
        initMainCircleMenuButton();

        // 調用實例化圖釘樣式選擇按鈕菜單的方法
        initSubCircleMenuButton();

        // 禁用地圖旋轉
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        // 從資料庫獲取已儲存的圖釘並存為 savedMarkerList
        getSavedMarkerFromDatabase();

        // 調用重載地圖並添加全部圖釘和折線的方法 (從資料庫獲取需要時間，因此需要在重載圖釘前設定一定的延遲)
        new Handler().postDelayed(() -> reloadMarker(), 1000);

        // 調用點擊行程連接線反轉色彩的方法
        onPolylineClick(mMap);

        // 調用點擊地圖關閉圓形按鈕菜單的方法
        onTouchMap();

        // 調用長按地圖添加一個新圖釘的方法
        onMapLongClick();

        // 調用點擊圖釘顯示圓形按鈕菜單的方法
        onMarkerClick();

        // 調用監測攝像機移動的方法
        onCameraMove();

        if (markerList.size() > 0) {
            // 將視點移動到(lat,lng)所在位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),18.0f));

            return; // 提前結束語句
        }

        if (tempMarkerList.size() == 0){
            // 將視點移動到文化大學所在位置
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(25.1364655,121.538592),18.0f));
        } else {
            // 將視點移動到(lat,lng)所在位置
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),18.0f));
        }
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void searchDataFromDatabase(Double targetLat, Double targetLng) { // 搜尋是否有圖釘的坐標存在於 DataBase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CostInfo");

        // 根據緯度和經度進行查詢
        Query query = ref.orderByChild("latitude").equalTo(targetLat);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 遍歷符合條件的每一筆資料
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // 檢查該筆資料的經度是否與目標經度相同
                    double longitude = childSnapshot.child("longitude").getValue(Double.class);
                    boolean isComplete = childSnapshot.child("isComplete").getValue(Boolean.class);
                    if (longitude == targetLng && isComplete == false) {
                        Toast.makeText(getActivity(),"此地點暫未添加消費資訊",Toast.LENGTH_SHORT).show();
                        PlaceInfo.costInfoKey = childSnapshot.getKey(); // 獲取當前資料在 CostInfo 中的索引
                        PlaceInfo.isComplete = false;
                        break;
                    }
                    if (longitude == targetLng && isComplete == true) {
                        Toast.makeText(getActivity(),"此地點已有完整的消費資訊",Toast.LENGTH_SHORT).show();
                        PlaceInfo.isComplete = true;
                        Map<String, Object> costInfoMap = new HashMap<>();
                        Double latTemp;
                        Double lngTemp;
                        String payerNameTemp;
                        String payerIdTemp;
                        String payerAvatarTemp;

                        // 找到符合條件的資料，進行相應的操作
                        getTargetCostInfo = true;
                        PlaceInfo.markerIndex = childSnapshot.child("markerIndex").getValue(Integer.class);
                        PlaceInfo.iconIndex = childSnapshot.child("markerIconIndex").getValue(Integer.class);
                        PlaceInfo.placeName = childSnapshot.child("placeName").getValue(String.class);
                        latTemp = childSnapshot.child("latitude").getValue(Double.class);
                        lngTemp = childSnapshot.child("longitude").getValue(Double.class);
                        PlaceInfo.location = new LatLng(latTemp, lngTemp);
                        PlaceInfo.date = childSnapshot.child("date").getValue(String.class);
                        PlaceInfo.hour = childSnapshot.child("hour").getValue(Integer.class);
                        PlaceInfo.minute = childSnapshot.child("minute").getValue(Integer.class);
                        PlaceInfo.itemName = childSnapshot.child("itemName").getValue(String.class);
                        PlaceInfo.expense = childSnapshot.child("expense").getValue(Integer.class);
                        payerNameTemp = childSnapshot.child("payerName").getValue(String.class);
                        payerIdTemp = childSnapshot.child("payerId").getValue(String.class);
                        payerAvatarTemp = childSnapshot.child("payerAvatar").getValue(String.class);
                        PlaceInfo.payer = new UserInfo(payerNameTemp, payerIdTemp, payerAvatarTemp);

                        // 獲取 participantInfoList
                        List<Map<String, Object>> participantInfoListMap = (List<Map<String, Object>>) childSnapshot.child("friendInfoList").getValue();

                        Toast.makeText(getActivity(),"地點名稱: "+PlaceInfo.placeName,Toast.LENGTH_SHORT).show();

                        // 將雲端的 friendInfoList 轉換為 ArrayList<UserInfo> participantInfoList (注意此兩個名詞有不同)
                        List<UserInfo> participantInfoList = new ArrayList<>();
                        for (Map<String, Object> participantInfoMap : participantInfoListMap) {
                            String name = (String) participantInfoMap.get("name");
                            String id = (String) participantInfoMap.get("id");
                            String avatar = (String) participantInfoMap.get("avatar");
                            UserInfo participantInfo = new UserInfo(name, id, avatar);
                            participantInfoList.add(participantInfo);
                        }

                        // 儲存 friendInfoList 到 PlaceInfo
                        PlaceInfo.participantInfoList = participantInfoList;

                        break; // 如果只需要取得第一筆符合條件的資料，可以使用 break 跳出循環
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤
                Toast.makeText(getActivity(),"未在資料庫找到指定圖釘",Toast.LENGTH_SHORT).show();
                getTargetCostInfo = false;
            }
        });
    }

    private void getSavedMarkerFromDatabase() {
        // 創建Firebase Realtime Database參考
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CostInfo");

        // 監聽Firebase Realtime Database中的數據
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 清除先前的標記
                //mMap.clear();

                // 清除先前儲存標記的 tempMarkerList 和 markerList
                tempMarkerList.clear();
                markerList.clear();

                // 給當前圖釘總數量賦值
                pointCount = (int) dataSnapshot.getChildrenCount();

                //Toast.makeText(getActivity(),"從資料庫獲取到已儲存的圖釘數量: "+pointCount,Toast.LENGTH_SHORT).show();

                // 遍歷Firebase Realtime Database中的所有數據
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // 將Firebase Realtime Database中的數據轉換為Java對象
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    int markerIconIndex = snapshot.child("markerIconIndex").getValue(Integer.class);
                    int markerIndex = snapshot.child("markerIndex").getValue(Integer.class);
                    int routeIndex = snapshot.child("routeIndex").getValue(Integer.class);

                    routeCount = routeIndex; // 更新使用者的總行程計數
                    markerCount = markerIndex + 1; // 更新當前路徑的圖釘計數

                    // 賦值給攝影機移動的指定坐標位置
                    lat = latitude;
                    lng = longitude;

                    // 添加標記到 ArrayList
                    tempMarkerList.add(new UserMarker(0, markerIndex, pointCount, markerIconIndex, latitude, longitude));
                }
                //Toast.makeText(getActivity(),"從資料庫獲取到已儲存的圖釘數量: "+tempMarkerList.size(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 出現錯誤時處理
                Toast.makeText(getActivity(),"未從資料庫獲取到已儲存的圖釘",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCostInfoFromDatabase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CostInfo");

        // 根據緯度和經度進行查詢
        Query query = ref.orderByChild("latitude").equalTo(lat);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 遍歷符合條件的每一筆資料
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // 檢查該筆資料的經度是否與目標經度相同
                    double longitude = childSnapshot.child("longitude").getValue(Double.class);
                    if (longitude == lng) {
                        // 從 Firebase Realtime Database 中刪除 childSnapshot 對應的節點
                        childSnapshot.getRef().removeValue();
                        //Toast.makeText(getActivity(),"已刪除對應的 CostInfo",Toast.LENGTH_SHORT).show();

                        break; // 如果只需要取得第一筆符合條件的資料，可以使用 break 跳出循環
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤
                Toast.makeText(getActivity(),"未在資料庫找到指定圖釘",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCostInfoFromDatabase() { // 從資料庫獲取全部 CostInfo,目前沒有使用
        // 創建 Firebase Realtime Database 參考
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("CostInfo");

        // 監聽 Firebase Realtime Database 中的數據
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 遍歷 Firebase Realtime Database 中的所有數據
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double latTemp;
                    Double lngTemp;
                    String payerNameTemp;
                    String payerIdTemp;
                    String payerAvatarTemp;

                    // 將 Firebase Realtime Database 中的數據轉換為Java對象
                    PlaceInfo.markerIndex = snapshot.child("markerIndex").getValue(Integer.class);
                    PlaceInfo.iconIndex = snapshot.child("markerIconIndex").getValue(Integer.class);
                    PlaceInfo.placeName = snapshot.child("placeName").getValue(String.class);
                    latTemp = snapshot.child("latitude").getValue(Double.class);
                    lngTemp = snapshot.child("longitude").getValue(Double.class);
                    PlaceInfo.location = new LatLng(latTemp, lngTemp);
                    PlaceInfo.date = snapshot.child("date").getValue(String.class);
                    PlaceInfo.hour = snapshot.child("hour").getValue(Integer.class);
                    PlaceInfo.minute = snapshot.child("minute").getValue(Integer.class);
                    PlaceInfo.itemName = snapshot.child("itemName").getValue(String.class);
                    PlaceInfo.expense = snapshot.child("expense").getValue(Integer.class);
                    payerNameTemp = snapshot.child("payerName").getValue(String.class);
                    payerIdTemp = snapshot.child("payerId").getValue(String.class);
                    payerAvatarTemp = snapshot.child("payerAvatar").getValue(String.class);
                    PlaceInfo.payer = new UserInfo(payerNameTemp, payerIdTemp, payerAvatarTemp);

                    // 獲取 friendInfoList
                    List<Map<String, Object>> friendInfoListMap = (List<Map<String, Object>>) snapshot.child("friendInfoList").getValue();

                    Toast.makeText(getActivity(),"地點名稱: "+PlaceInfo.placeName,Toast.LENGTH_SHORT).show();

                    // 將雲端的 friendInfoList 轉換為 ArrayList<UserInfo> friendInfoList
                    List<UserInfo> friendInfoList = new ArrayList<>();
                    for (Map<String, Object> friendInfoMap : friendInfoListMap) {
                        String name = (String) friendInfoMap.get("name");
                        String id = (String) friendInfoMap.get("id");
                        String avatar = (String) friendInfoMap.get("avatar");
                        UserInfo friendInfo = new UserInfo(name, id, avatar);
                        friendInfoList.add(friendInfo);
                    }

                    // 儲存 friendInfoList 到 PlaceInfo
                    PlaceInfo.friendInfoList = friendInfoList;

                    break; // 如果只需要取得第一筆符合條件的資料，可以使用 break 跳出循環
                }
                Toast.makeText(getActivity(),"成功獲取到一筆資料",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 出現錯誤時處理
                Toast.makeText(getActivity(),"未獲取到資料",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 向雲端添加一筆只包含圖釘資料的 CostInfo
    public void sendNullCostInfoToFireBase(UserMarker userMarker) {
        LatLng location = new LatLng(userMarker.lat, userMarker.lng); // 坐標 LatLng(double Lat, double Lng)
        int routeIndex = userMarker.routeIndex; // 在一條默認行程中所對應的路徑的 Index
        int markerIndex = userMarker.markerIndex; // 在一條路徑中所對應的圖釘的 Index
        int iconIndex = userMarker.markerIconIndex; // 圖釘的 Icon 序號

        Map<String, Object> costInfoMap = new HashMap<>();
        costInfoMap.put("isComplete", false); // 默認為 false
        costInfoMap.put("routeIndex", routeIndex);
        costInfoMap.put("markerIndex", markerIndex);
        costInfoMap.put("markerIconIndex", iconIndex);
        costInfoMap.put("placeName", "null");
        costInfoMap.put("latitude", location.latitude);
        costInfoMap.put("longitude", location.longitude);
        costInfoMap.put("userCount", 0);
        costInfoMap.put("date", "null");
        costInfoMap.put("hour", 0);
        costInfoMap.put("minute", 0);
        costInfoMap.put("itemName", "null");
        costInfoMap.put("expense", 0);

        costInfoMap.put("payerName", "null");
        costInfoMap.put("payerId", "null");
        costInfoMap.put("payerAvatar", "null");

        costInfoMap.put("groupName", "null");
        costInfoMap.put("groupAvatar", "null");

        // 將 friendInfoList 轉換為 List<Map<String, Object>> 並添加到 groupInfoMap 中
        List<Map<String, Object>> friendInfoListMap = new ArrayList<>();
        for (UserInfo friendInfo : PlaceInfo.friendInfoList) {
            Map<String, Object> friendInfoMap = new HashMap<>();
            friendInfoMap.put("name", "null");
            friendInfoMap.put("id", "null");
            friendInfoMap.put("avatar", "null");
            friendInfoListMap.add(friendInfoMap);
        }
        costInfoMap.put("friendInfoList", friendInfoListMap);

        // 使用 push() 方法將新的 GroupInfo 寫入資料庫
        ref.push().setValue(costInfoMap);
    }

    // 實例化地點搜索功能 Bar
    private void initSearchBar() {
        Places.initialize(getContext(),"AIzaSyBgHNt_cwE4HGzLhPBvwRWjBW8Wg9AEWwY");

        mEditText.setFocusable(false);
        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList)
                        .build(getContext());
                startActivityForResult(intent,100);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);

            mEditText.setText(place.getAddress());
            //PlaceInfo.placeName = place.getName();

            //mTextView01.setText(String.format("地點名稱: %s",place.getName()));
            //mTextView02.setText(String.valueOf(place.getLatLng()));

            // 將視角移動至搜尋到的坐標位置
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(place.getLatLng(),18.0f));

            // 添加一個圖釘
            //new Handler().postDelayed(() -> onAddButtonClick(), 1000);

            // 檢查當前 LatLng 對應的地點是否已經有 CostInfo 存在
            //new Handler().postDelayed(() -> searchDataFromDatabase(), 1000);

            // 展開圓形展開按鈕主菜單
            //new Handler().postDelayed(() -> openMainCircleMenu(mainMenu), 1000);

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getActivity(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE, true);
        // [END maps_check_location_permission]
    }

    // 點擊當前位置按鈕事件
    private void initLocationButton(){
        client = LocationServices.getFusedLocationProviderClient(getActivity());
        mLocationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // When permission is granted
                    getCurrentLocation();
                } else {
                    // When permission is not granted
                    // Request permission
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},100);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check condition
        if(requestCode == 100 && (grantResults.length>0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            // When permission are granted
            getCurrentLocation();
        } else {
            // When permission are denied
            Toast.makeText(getActivity(),"獲取位置權限被拒絕",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(){
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location originalLocation = task.getResult(); // 初始目標經緯度

                    if (originalLocation != null) {
                        // 將視角移動至使用者的坐標位置
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(new LatLng(originalLocation.getLatitude(), originalLocation.getLongitude()),18.0f));

                        // 暫時禁用使用者的地圖拖動手勢(目的是使圖釘固定在畫面中心)
                        //disableScrollMap();

                        // 調用展開圓形展開按鈕主菜單的方法
                        //new Handler().postDelayed(() -> onAddButtonClick(), 1500);

                        // 檢查當前 LatLng 對應的地點是否已經有 CostInfo 存在
                        //new Handler().postDelayed(() -> searchDataFromDatabase(), 1500);

                        //new Handler().postDelayed(() -> openMainCircleMenu(mainMenu), 1500);
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location originalLocation = locationResult.getLastLocation();

                                // 將視角移動至使用者的坐標位置
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(new LatLng(originalLocation.getLatitude(), originalLocation.getLongitude()),18.0f));

                                // 暫時禁用使用者的地圖拖動手勢(目的是使圖釘固定在畫面中心)
                                //disableScrollMap();

                                // 調用展開圓形展開按鈕主菜單的方法
                                //new Handler().postDelayed(() -> onAddButtonClick(), 1500);
                                //new Handler().postDelayed(() -> openMainCircleMenu(mainMenu), 1500);
                            }
                        };
                        // Request Location updates
                        client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        }
        else {
            // 若 Location Service 沒有開啓
            // 則打開 Location Setting 開啓權限
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    // 點擊圖釘事件
    //private void onMarkerClick()
    private boolean onMarkerClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);

        mMap.setOnMarkerClickListener(marker -> {
            closeAllMenu(); // 關閉並隱藏所有菜單

            // 獲取當前圖釘的坐標
            lat = marker.getPosition().latitude;
            lng = marker.getPosition().longitude;

            // 以點擊的圖釘為中心移動畫面, moveCamera為瞬間移動, animateCamera有緩衝動畫效果
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));

            disableScrollMap(); // 暫時禁用使用者的地圖拖動手勢(目的是使圖釘固定在畫面中心)

            // 檢查當前 LatLng 對應的地點是否已經有 CostInfo 存在
            new Handler().postDelayed(() -> searchDataFromDatabase(lat,lng), 1080);

            // 展開主要按鈕菜單
            new Handler().postDelayed(() -> openMainCircleMenu(mainMenu), 1080);

            // 顯示當前所點擊圖釘的資訊（注意：若圖釘不在畫面中心，則必須等待攝像機移動完畢，直到圖釘位於畫面正中心）
            new Handler().postDelayed(() -> showMarkerInfo(), 1080);
/*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 延遲要執行的程式碼
                    Toast.makeText(getActivity(),"地點坐標: "+"("+targetLatitude+","+targetLongitude+")",Toast.LENGTH_SHORT).show();
                }
            }, 1080);
*/
            return true;
        });
        return true;
    }

    // 長按地圖獲取經緯度數值, 並添加臨時圖釘到mMarker
    //private void onMapLongClick()
    private boolean onMapLongClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        mMap.setOnMapLongClickListener(latLng -> {
            //Toast.makeText(getActivity(),"地圖被長按",Toast.LENGTH_SHORT).show();
            closeAllMenu(); // 關閉並隱藏所有菜單

            // 獲取當前圖釘的坐標
            lat = latLng.latitude;
            lng = latLng.longitude;

            // 如果使用者通過長按地圖的方式添加新圖釘，則清空之前通過搜索地點功能獲取到的地點名稱
            PlaceInfo.placeName = null;

            // 以添加的圖釘為中心移動畫面, moveCamera為瞬間移動, animateCamera有緩衝動畫效果
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));

            disableScrollMap(); // 暫時禁用使用者的地圖拖動手勢(目的是使圖釘固定在畫面中心)

            // 調用展開圓形展開按鈕主菜單的方法
            new Handler().postDelayed(() -> onAddButtonClick(), 1000);
            new Handler().postDelayed(() -> openMainCircleMenu(mainMenu), 1000);
        });
        return true;
    }

    // 點擊地圖事件
    private void onTouchMap() {
        mMap.setOnMapClickListener(latLng -> {
            //Toast.makeText(getActivity(),"地圖被點擊",Toast.LENGTH_SHORT).show();
            closeAllMenu(); // 關閉並隱藏所有菜單
        });
    }

    // 監測攝像機移動（移動地圖、縮放地圖），若有移動則關閉圓形按鈕菜單
    private void onCameraMove() {
        mMap.setOnCameraMoveStartedListener (new GoogleMap.OnCameraMoveStartedListener() {
            public void onCameraMoveStarted(int i) {
                closeAllMenu(); // 關閉並隱藏所有菜單
            }
        });
    }

    // 關閉并隱藏所有展開中的菜單
    private void closeAllMenu() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        final CircleMenuView subMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_sub);
        mainMenu.close(false);
        mainMenu.setVisibility(View.INVISIBLE);
        subMenu.close(false);
        subMenu.setVisibility(View.INVISIBLE);
        enableScrollMap(); // 恢復地圖拖動手勢
    }

    // 清除當前圖釘並重新載入全部圖釘(延遲0.8秒)
    private void reloadMarker() {
        getTargetCostInfo = false;
        PlaceInfo.costInfoKey = "";
        PlaceInfo.isComplete = false;
        mMap.clear();
        //addAllMarker(savedMarkerList);
        addAllMarker(markerList);
        addAllMarker(tempMarkerList);
        addAllPolyline(polylineList);
    }

    // 檢測圖釘是否位於地圖中心 (未使用，未更新的舊方法)
    private boolean markerInCenter(){
        boolean markerInCenter = false;
        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(mMap.getCameraPosition().target.latitude == markerList.get(i).lat && mMap.getCameraPosition().target.longitude == markerList.get(i).lng){
                markerInCenter = true;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(mMap.getCameraPosition().target.latitude == tempMarkerList.get(i).lat && mMap.getCameraPosition().target.longitude == tempMarkerList.get(i).lng){
                markerInCenter = true;
            }
        }

        if (markerInCenter == true){
            return true;
        }
        else {
            return false;
        }
    }

    private void hideSoftKeyboard(){ // 隱藏用戶鍵盤輸入 (未使用)
        Utils.hideKeyboard(getActivity());
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // 實例化圓形展開按鈕菜單
    private void initMainCircleMenuButton() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);

        closeCircleMenu(mainMenu);
        mainMenu.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationStart");
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationEnd");
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationStart");
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationEnd");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                // 圓形展開按鈕菜單的子菜單點擊事件在此設置
                if(index == 0){
                    onEditButtonClick();
                }
                if(index == 1){
                    onInfoButtonClick();
                }
                if(index == 2){
                    onDeleteButtonClick();
                }
                if(index == 3){
                    onSaveButtonClick();
                    // 隱藏已被創建的圓形按鈕菜單(延遲0.8秒，等待動畫結束)
                    new Handler().postDelayed(() -> mainMenu.setVisibility(View.INVISIBLE), 800);
                }
                Log.d("D", "onButtonClickAnimationStart| index: " + index);
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationEnd| index: " + index);
            }
        });
    }

    // 顯示使用者所選定圖釘的資訊（注意：若攝像機移動尚未完成，則必須設定一個執行延遲，等待攝像機移動完畢）
    private void showMarkerInfo(){
        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng){
                Toast.makeText(getActivity(),"儲存圖釘總數量: "+markerList.size()+" | "+"行程序號: "+markerList.get(i).getRouteIndex()+" | "+"地點序號: "+markerList.get(i).getMarkerIndex(),Toast.LENGTH_SHORT).show();
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                Toast.makeText(getActivity(),"圖釘總數量: "+tempMarkerList.size()+" | "+"行程序號: "+tempMarkerList.get(i).getRouteIndex()+" | "+"地點序號: "+tempMarkerList.get(i).getMarkerIndex(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 綠色的添加圖釘按鈕被點擊事件
    private void onAddButtonClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        boolean markerIsExist = false;
        boolean markerIsTemp = false;
        int randomMarkerIcon = (int)(Math.random() * 3 + 1); // 跳過序號為0的GoogleMap默認圖釘，隨機添加一個圖釘

        // 自定義圖釘圖標（使用vector格式會報錯，目前使用png格式）
        BitmapDescriptor defaultMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        BitmapDescriptor redMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_red);
        BitmapDescriptor yellowMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_yellow);
        BitmapDescriptor greenMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
        BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_blue);
        BitmapDescriptor starMarkerIcon01 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_01);
        BitmapDescriptor starMarkerIcon02 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_02);
        BitmapDescriptor starMarkerIcon03 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_03);

        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = false;
                mainMenu.close(false);
                mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
                enableScrollMap();
                Toast.makeText(getActivity(),"請選擇一個新地點添加圖釘",Toast.LENGTH_SHORT).show();
                break;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = true;
                mainMenu.close(false);
                mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
                enableScrollMap();
                Toast.makeText(getActivity(),"請選擇一個新地點添加圖釘",Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (markerIsExist == false){
            UserMarker userMarker = new UserMarker(0, markerCount, 0, randomMarkerIcon, lat, lng);
            tempMarkerList.add(userMarker);

            //sendNullCostInfoToFireBase(userMarker); // 向雲端添加一筆只包含圖釘資料的 CostInfo

            showMarkerInfo(); // 顯示剛才添加圖釘的資訊
            markerCount++;

            // 清除當前圖釘並重新載入全部圖釘
            reloadMarker();
        }
    }

    // 黃色的修改圖釘按鈕被點擊事件
    private void onEditButtonClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        final CircleMenuView subMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_sub);
        boolean markerIsExist = false;
        boolean markerIsTemp = false;

        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng) {
                markerIsExist = true;
                markerIsTemp = false;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = true;
                mainMenu.close(false);
                mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
                //initSubCircleMenuButton(); // 實例化圖釘樣式選擇菜單
                openSubCircleMenu(subMenu); // 打開圖釘樣式選擇菜單
                Toast.makeText(getActivity(),"編輯圖釘樣式",Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (markerIsExist == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"圖釘的坐標與雲端不匹配",Toast.LENGTH_SHORT).show();
        }

        if (markerIsExist == true && markerIsTemp == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"目前僅能修改臨時圖釘",Toast.LENGTH_SHORT).show();
        }
    }

    // 紅色的刪除圖釘按鈕被點擊事件
    private void onDeleteButtonClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        boolean markerIsExist = false;
        boolean markerIsTemp = false;

        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng) {
                markerIsExist = true;
                markerIsTemp = false;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = true;

                // 從markerList中刪除特定圖釘
                if(tempMarkerList.get(i).getRouteIndex() == 0){ // 判定是否正在刪除臨時行程中的地點，0則代表為臨時行程
                    tempMarkerList.remove(i);
                    //markerCount--;
                }
                else{ // 判定是否正在刪除正式行程中的地點（待完善）+++++++++++++++++++
                    markerCount = tempMarkerList.get(i).getMarkerIndex();
                    tempMarkerList.remove(i);
                }
            }
        }

        if (markerIsExist == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"圖釘的坐標與雲端不匹配",Toast.LENGTH_SHORT).show();
        }

        if (markerIsExist == true && markerIsTemp == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"目前僅能刪除臨時圖釘",Toast.LENGTH_SHORT).show();
        }

        if (markerIsExist == true && markerIsTemp == true) {
            // 如果使用者刪除某一個圖釘，則清空之前通過搜索地點功能獲取到的地點名稱
            PlaceInfo.placeName = null;

            // 若資料庫存在指定圖釘，則連同資料庫的對應 CostInfo 一起刪除
            deleteCostInfoFromDatabase();

            // 清除當前圖釘並重新載入全部圖釘(延遲0.8秒)
            new Handler().postDelayed(() -> reloadMarker(), 800);
            new Handler().postDelayed(() -> mainMenu.close(false), 800);
            new Handler().postDelayed(() -> mainMenu.setVisibility(View.INVISIBLE), 800);
            new Handler().postDelayed(() -> enableScrollMap(), 800);
            Toast.makeText(getActivity(),"刪除新增的圖釘",Toast.LENGTH_SHORT).show();
        }
    }

    private void onInfoButtonClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        boolean markerIsExist = false;
        boolean markerIsTemp = false;

        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng) {
                markerIsExist = true;
                markerIsTemp = false;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = true;
                // 獲取當前圖釘的 routeIndex, markerIndex, iconIndex 和 LatLng 並賦值給地點資訊 PlaceInfo
                PlaceInfo.routeIndex = tempMarkerList.get(i).getRouteIndex();
                PlaceInfo.markerIndex = tempMarkerList.get(i).getMarkerIndex();
                PlaceInfo.iconIndex = tempMarkerList.get(i).getMarkerIconIndex();
                PlaceInfo.location = new LatLng(tempMarkerList.get(i).getLat(),tempMarkerList.get(i).getLng());
            }
        }

        if (getTargetCostInfo == false) {
            if (markerIsExist == false) {
                mainMenu.close(false);
                mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
                enableScrollMap();
                Toast.makeText(getActivity(),"圖釘的坐標與雲端不匹配",Toast.LENGTH_SHORT).show();
            }

            if (markerIsExist == true && markerIsTemp == false) {
                //
            }

            if (markerIsExist == true && markerIsTemp == true) {
                /*
                Toast.makeText(getActivity(),"編輯中的地點序號: "+PlaceInfo.markerIndex+"\n"
                                +"坐標: "+PlaceInfo.location,
                        Toast.LENGTH_SHORT).show();
                */
                new Handler().postDelayed(() -> closeAllMenu(), 800);
                //new Handler().postDelayed(() -> navController.navigate(R.id.action_navigation_map_to_groupSelectFragment), 800);
                // 進入消費資訊編輯頁面
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 延遲要執行的程式碼
                        // Fragment 切換
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host, new GroupSelectFragment());
                        fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
                        fragmentTransaction.addToBackStack(null); //可選，將該操作添加到返回堆疊中
                        fragmentTransaction.commit();
                    }
                }, 800);
                //Toast.makeText(getActivity(),"進入消費資訊編輯界面",Toast.LENGTH_SHORT).show();
            }
        } else {
            // 跳轉並顯示之前儲存的 CostInfo
            new Handler().postDelayed(() -> closeAllMenu(), 800);
            //new Handler().postDelayed(() -> navController.navigate(R.id.action_navigation_map_to_costInfoShowFragment), 800);
            // 進入消費資訊顯示頁面
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 延遲要執行的程式碼
                    // Fragment 切換
                    fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host, new CostInfoShowFragment());
                    fragmentTransaction.setTransition(TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.addToBackStack(null); //可選，將該操作添加到返回堆疊中
                    fragmentTransaction.commit();
                }
            }, 800);
            Toast.makeText(getActivity(),"顯示當前地點的消費資訊",Toast.LENGTH_SHORT).show();
        }
    }

    private void onSaveButtonClick() {
        final CircleMenuView mainMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_main);
        boolean markerIsExist = false;
        boolean markerIsTemp = false;
        int pointCount = 0; // 至少有一個圖釘

        // 在全部圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for(int i = 0; i < markerList.size(); i++){
            if(lat == markerList.get(i).lat && lng == markerList.get(i).lng) {
                markerIsExist = true;
                markerIsTemp = false;
            }
        }

        // 在臨時圖釘中尋找是否有圖釘和攝影機當前坐標一致，若一致則視爲該圖釘被點擊,不一致則視爲沒有找到指定圖釘
        for (int i=0; i<tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                markerIsExist = true;
                markerIsTemp = true;

                int finalI = i;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 延遲要執行的程式碼
                        UserMarker userMarker = new UserMarker(0, markerCount, 0, tempMarkerList.get(finalI).markerIconIndex, tempMarkerList.get(finalI).lat, tempMarkerList.get(finalI).lng);
                        //tempMarkerList.add(userMarker);

                        sendNullCostInfoToFireBase(userMarker); // 向雲端添加一筆只包含圖釘資料的 CostInfo

                        showMarkerInfo(); // 顯示剛才添加圖釘的資訊
                        //markerCount++;

                        // 清除當前圖釘並重新載入全部圖釘
                        reloadMarker();

                        Toast.makeText(getActivity(),"圖釘坐標已儲存到雲端",Toast.LENGTH_SHORT).show();
                    }
                }, 800);

                /*
                markerCount = 0; // 初始化單次行程内的圖釘序號（為下次添加行程做準備）
                routeCount++; // 使用者的總行程計數增加1


                // 為 tempMarkerList 中的圖釘添加pointCount
                for (int j = 0; j < tempMarkerList.size(); j++) {
                    if (tempMarkerList.get(j).getRouteIndex() == 0) {
                        pointCount++;
                    }
                }

                for (int j = 0; j < tempMarkerList.size(); j++) {
                    if (tempMarkerList.get(j).getRouteIndex() == 0) {
                        tempMarkerList.get(j).setRouteIndex(routeCount); // 變更指定圖釘的行程序號,增加1
                        tempMarkerList.get(j).setPointCount(pointCount); // 變更指定圖釘的pointCount
                        markerList.add(new UserMarker( // 將臨時圖列表釘的資料轉移到圖釘列表
                                tempMarkerList.get(j).getRouteIndex(),
                                tempMarkerList.get(j).getMarkerIndex(),
                                tempMarkerList.get(j).getPointCount(),
                                tempMarkerList.get(j).getMarkerIconIndex(),
                                tempMarkerList.get(j).getLat(),
                                tempMarkerList.get(j).getLng()));
                        polylineList.add(new UserPolyline(
                                tempMarkerList.get(j).getRouteIndex(),
                                tempMarkerList.get(j).getPointCount(),
                                new LatLng(tempMarkerList.get(j).getLat(), tempMarkerList.get(j).getLng())));
                    }
                }

                // 清除當前圖釘並重新載入全部圖釘(延遲0.8秒)
                new Handler().postDelayed(() -> reloadMarker(), 800);

                //Toast.makeText(getActivity(),"點擊了儲存行程按鈕",Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(),"儲存的圖釘數量: "+pointCount,Toast.LENGTH_SHORT).show();

                pointCount = 0;
                tempMarkerList.clear();
                */
            }
        }

        if (markerIsExist == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"圖釘的坐標與雲端不匹配",Toast.LENGTH_SHORT).show();
        }

        if (markerIsExist == true && markerIsTemp == false) {
            mainMenu.close(false);
            mainMenu.setVisibility(View.INVISIBLE); // 隱藏主菜單
            enableScrollMap();
            Toast.makeText(getActivity(),"目前僅能儲存臨時圖釘",Toast.LENGTH_SHORT).show();
        }
    }

    // 實例化圓形展開按鈕菜單
    private void initSubCircleMenuButton() {
        final CircleMenuView subMenu = (CircleMenuView) mView.findViewById(R.id.circle_menu_sub);
        closeCircleMenu(subMenu);
        subMenu.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationStart");
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationEnd");
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationStart");
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationEnd");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                // 圓形展開按鈕菜單的子菜單點擊事件在此設置
                if(index == 0){ // index的值為0，初始按鈕在12點鐘方向
                    changeMarkerIcon(1);
                }
                if(index == 1){
                    changeMarkerIcon(2);
                }
                if(index == 2){
                    changeMarkerIcon(3);
                }
                Log.d("D", "onButtonClickAnimationStart| index: " + index);
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationEnd| index: " + index);
            }
        });
    }

    // 圓形按鈕菜單（主菜單）展開
    private void openMainCircleMenu(CircleMenuView menu) {
        if (onMarkerClick()){ // 點擊圖釘後展開圓形按鈕菜單
            // 取消隱藏已被創建的圓形按鈕菜單
            menu.setVisibility(View.VISIBLE);
            // 展開弧形按鈕菜單
            menu.open(true);
        }

        if (onMapLongClick()){ // 長按地圖上某一地點後展開圓形按鈕菜單
            //circleMenuButtonExpanded = true;
            // 取消隱藏已被創建的圓形按鈕菜單
            menu.setVisibility(View.VISIBLE);
            // 展開弧形按鈕菜單
            menu.open(true);
        }
    }

    // 在 markerList 中尋找指定的圖釘，並改變圖釘Icon序號
    private void changeMarkerIcon(int markerIconIndex){
        for(int i = 0; i < tempMarkerList.size(); i++){
            if(lat == tempMarkerList.get(i).lat && lng == tempMarkerList.get(i).lng){
                tempMarkerList.get(i).setMarkerIconIndex(markerIconIndex);
                changeMarkerIconFromDataBase(markerIconIndex);
                Toast.makeText(getActivity(),"已修改圖釘樣式，icon序號為: " + markerIconIndex,Toast.LENGTH_SHORT).show();
                break;
            }
        }
        new Handler().postDelayed(() -> reloadMarker(), 800);
        /*
        if(mMap.getCameraPosition().target.latitude != markerList.get(i).lat && mMap.getCameraPosition().target.longitude != markerList.get(i).lng){
            Toast.makeText(getActivity(),"沒有找到可以修改的圖釘",Toast.LENGTH_SHORT).show();
        }
        */
    }

    private void changeMarkerIconFromDataBase(int markerIconIndex) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("CostInfo");

        // 根據緯度和經度進行查詢
        Query query = ref.orderByChild("latitude").equalTo(lat);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 遍歷符合條件的每一筆資料
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // 檢查該筆資料的經度是否與目標經度相同
                    double longitude = childSnapshot.child("longitude").getValue(Double.class);
                    if (longitude == lng) {
                        // 從 Firebase Realtime Database 中更新 markerIconIndex 屬性的值
                        childSnapshot.getRef().child("markerIconIndex").setValue(markerIconIndex);
                        //Toast.makeText(getActivity(),"已修改 CostInfo 對應的圖釘樣式",Toast.LENGTH_SHORT).show();

                        break; // 如果只需要取得第一筆符合條件的資料，可以使用 break 跳出循環
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 處理錯誤
                Toast.makeText(getActivity(),"未在資料庫找到指定圖釘",Toast.LENGTH_SHORT).show();
                //getTargetCostInfo = false;
            }
        });

    }

    // 圓形按鈕菜單（圖釘選擇菜單）展開
    private void openSubCircleMenu(CircleMenuView menu) {
        menu.setVisibility(View.VISIBLE);// 點擊修改圖釘樣式按鈕後，展開圖釘樣式選擇按鈕菜單
        menu.open(true);// 展開弧形按鈕菜單
    }

    // 圓形按鈕菜單關閉（適用於全部）
    private void closeCircleMenu(CircleMenuView menu) {
        mMap.setOnMarkerClickListener(marker -> { // 判定是否點擊地圖上某一圖釘
            menu.close(true); // 關閉弧形按鈕菜單
            menu.setVisibility(View.INVISIBLE); // 隱藏已被創建的圓形按鈕菜單
            enableScrollMap(); // 調用啓用地圖的縮放移動的方法
            return true;
        });

        mMap.setOnMapClickListener(latLng -> { // 判定是否點擊地圖上某一地點
            menu.close(true); // 關閉弧形按鈕菜單
            menu.setVisibility(View.INVISIBLE); // 隱藏已被創建的圓形按鈕菜單
            enableScrollMap(); // 調用啓用地圖的縮放移動的方法
        });

        mMap.setOnMapLongClickListener(latLng -> { // 判定是否長按地圖上某一地點
            menu.close(true); // 關閉弧形按鈕菜單
            menu.setVisibility(View.INVISIBLE); // 隱藏已被創建的圓形按鈕菜單
            enableScrollMap(); // 調用啓用地圖的縮放移動的方法
        });

        // 判定是否移動攝像機位置（移動地圖、縮放地圖），若有移動則關閉圓形按鈕菜單
        mMap.setOnCameraMoveStartedListener (new GoogleMap.OnCameraMoveStartedListener() {
            public void onCameraMoveStarted(int i) {
                menu.close(true); // 關閉弧形按鈕菜單
                menu.setVisibility(View.INVISIBLE);
                enableScrollMap();// 調用啓用地圖的縮放移動的方法
            }
        });
    }

    // 圓形按鈕菜單展開後暫時禁用地圖的縮放移動
    private void disableScrollMap() {
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        //Toast.makeText(getActivity(),"地圖的縮放移動已被禁用",Toast.LENGTH_SHORT).show();
    }

    // 啓用地圖的縮放移動
    private void enableScrollMap() {
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        //Toast.makeText(getActivity(),"地圖的縮放移動已被啓用",Toast.LENGTH_SHORT).show();
    }

    // 從列表中獲取並繪製全部圖釘
    private void addAllMarker(List<UserMarker> markerList){
        // 自定義圖釘圖標（使用vector格式會報錯，目前使用png格式）
        BitmapDescriptor defaultMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        BitmapDescriptor redMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_red);
        BitmapDescriptor yellowMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_yellow);
        BitmapDescriptor greenMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
        BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker_blue);
        BitmapDescriptor starMarkerIcon01 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_01);
        BitmapDescriptor starMarkerIcon02 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_02);
        BitmapDescriptor starMarkerIcon03 = BitmapDescriptorFactory.fromResource(R.drawable.marker_star_03);

        for(int i=0; i<markerList.size(); i++){
            //將從列表中獲取的坐標賦值給location, 儲存為LatLng形式
            LatLng location = new LatLng(markerList.get(i).getLat(), markerList.get(i).getLng());
            if (markerList.get(i).markerIconIndex == 0){
                //以location的坐標繪製一個圖釘
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(defaultMarkerIcon));
            }
            if (markerList.get(i).markerIconIndex == 1){
                //以location的坐標繪製一個圖釘
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(starMarkerIcon01));
            }
            if (markerList.get(i).markerIconIndex == 2){
                //以location的坐標繪製一個圖釘
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(starMarkerIcon02));
            }
            if (markerList.get(i).markerIconIndex == 3){
                //以location的坐標繪製一個圖釘
                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(starMarkerIcon03));
            }

            // 將視點移動到此圖釘位置
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.latitude, location.longitude),18.0f));

            // 重新設定加載地圖時攝影機移動的坐標
            lat = location.latitude;
            lng = location.longitude;
        }
    }

    // 從列表中獲取並繪製全部折線
    private void addAllPolyline(List<UserPolyline> polylineList){
        List<PatternItem> pattern = Arrays.asList(
                new Dot(), new Gap(20), new Dash(30), new Gap(20));

        // 繪製已儲存的行程（此處為實線）
        // 若新增一筆默認儲存的行程，routeIndex 若爲1，則 currentRouteIndex 為2 (比之前多+1)
        for (int i = 0, j = 0, currentRouteIndex = 1; i < polylineList.size(); i++) {
            // 判定具有相同指定路徑編號( )的圖釘的數量（currentRouteIndex從1開始）

            if (polylineList.get(i).getRouteIndex() == currentRouteIndex && j < polylineList.get(i).getPointCount()) {
                latLngList.add(polylineList.get(i).getLatLng());
                j++;
            }
            if (polylineList.get(i).getRouteIndex() == currentRouteIndex && j >= polylineList.get(i).getPointCount()) {
                // 折線的相關參數在此設置
                mMap.addPolyline(new PolylineOptions()
                        .addAll(latLngList)
                        .color(Color.BLACK)
                        .width(12)
                        .jointType(JointType.ROUND)
                        .endCap(new RoundCap())
                        .clickable(true));
                //Toast.makeText(getActivity(),"路徑已繪製，圖釘數量: "+latLngList.size(),Toast.LENGTH_SHORT).show();
                latLngList.clear();
                j = 0;

                currentRouteIndex++; // 準備繪製下一條路徑
            }
        }

        // 繪製臨時行程（此處為虛線）
        for (int i = 0, j = 0, currentRouteIndex = 0; i < tempMarkerList.size(); i++) {
            //將從列表中獲取的坐標賦值給location, 儲存為LatLng形式
            LatLng location = new LatLng(tempMarkerList.get(i).getLat(), tempMarkerList.get(i).getLng());
            if (tempMarkerList.get(i).getRouteIndex() == currentRouteIndex) {
                latLngList.add(location);
            }
        }
        // 折線的相關參數在此設置（此處為實線）
        mMap.addPolyline(new PolylineOptions()
                .addAll(latLngList)
                .color(Color.BLACK)
                .width(12)
                .jointType(JointType.ROUND)
                .endCap(new RoundCap())
                .pattern(pattern)
                .clickable(true));
        latLngList.clear();
    }

    private void onPolylineClick(GoogleMap googleMap){
        // Add a listener for polyline clicks that changes the clicked polyline's color.
        mMap.setOnPolylineClickListener(polyline -> {
            // Flip the values of the red, green and blue components of the polyline's color.
            polyline.setColor(polyline.getColor() ^ 0x00ffffff);
            Toast.makeText(getActivity(),"行程連接線被點擊",Toast.LENGTH_SHORT).show();
        });
    }

    // 設置回退鍵事件監聽器
    @Override
    public void onResume() {
        super.onResume();
        // 在 onResume() 中設定回退鍵事件監聽
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 在這裡撰寫回退鍵觸發事件
                // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                if (!isAdded()) {
                    return;
                }
                // Fragment 切換
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host, new MapFragment());
                fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
    }
}