package com.example.tmpdevelop_d;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfirmationFragment extends Fragment implements View.OnClickListener {


    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX
    public View view = null;
    private TextView textView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("CostInfo").child(PlaceInfo.costInfoKey);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_confirmation, container, false);
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("確認消費資訊");
        }
        setCostInfoTextView();
        return view;
    }

    private void setCostInfoTextView() {
        int markerIndex = PlaceInfo.markerIndex; // 地點圖釘序號
        String placeName = PlaceInfo.placeName; // 地點名稱
        LatLng location = PlaceInfo.location; // 地點坐標
        String date = PlaceInfo.date; // 消費日期
        int hour = PlaceInfo.hour; // 消費時間對應的小時
        int minute = PlaceInfo.minute; // 消費時間對應的分鐘
        String timeString = String.format(Locale.getDefault(),"%02d:%02d",hour,minute);
        String itemName = PlaceInfo.itemName; // 消費的項目
        int expense = PlaceInfo.expense; // 消費的價格
        UserInfo payer = PlaceInfo.payer; // 付款者的 UserInfo
        String payerName = payer.getUserName(); // 付款者的名字
        List<UserInfo> participantInfoList = new ArrayList<>(); // 參與消費的使用者清單
        participantInfoList.addAll(PlaceInfo.participantInfoList);
        int userCount = participantInfoList.size(); // 參與消費的使用者的人數

        List<String> participantNameList = new ArrayList<String>(); // 從 friendInfoList 中提取出參加行程的使用者的名字
        for (UserInfo participantInfo : participantInfoList) {
            participantNameList.add(participantInfo.getUserName());
        }
        String participantNames = String.join("; ", participantNameList); // 將參與消費的使用者的名字之間用分號隔開並轉換為同一個字符串

        textView = view.findViewById(R.id.confirmation_message);
        textView.setText("消費資訊已儲存"+"\n"+"\n"
                +"地點名稱: "+placeName+"\n"
                +"緯度: "+location.latitude+"\n"
                +"經度: "+location.longitude+"\n"+"\n"
                +"參與者: "+participantNames+"\n"
                +"人數: "+userCount+" 人"+"\n"
                +"消費日期: "+date+"\n"
                +"消費時間: "+timeString+"\n"
                +"消費項目: "+itemName+"\n"
                +"消費金額: "+expense+" 元"+"\n"
                +"付款者: "+payerName
        );
    }

    public void sendDataToFireBase() {
        Map<String, Object> costInfoMap = new HashMap<>();
        costInfoMap.put("isComplete", true);
        costInfoMap.put("routeIndex", PlaceInfo.routeIndex);
        costInfoMap.put("markerIndex", PlaceInfo.markerIndex);
        costInfoMap.put("markerIconIndex", PlaceInfo.iconIndex);
        costInfoMap.put("placeName", PlaceInfo.placeName);
        costInfoMap.put("latitude", PlaceInfo.location.latitude);
        costInfoMap.put("longitude", PlaceInfo.location.longitude);
        costInfoMap.put("userCount", PlaceInfo.participantInfoList.size());
        costInfoMap.put("date", PlaceInfo.date);
        costInfoMap.put("hour", PlaceInfo.hour);
        costInfoMap.put("minute", PlaceInfo.minute);
        costInfoMap.put("itemName", PlaceInfo.itemName);
        costInfoMap.put("expense", PlaceInfo.expense);

        costInfoMap.put("payerName", PlaceInfo.payer.getUserName());
        costInfoMap.put("payerId", PlaceInfo.payer.getUserId());
        costInfoMap.put("payerAvatar", PlaceInfo.payer.getAvatar());

        costInfoMap.put("groupName", PlaceInfo.groupInfo.getGroupName());
        costInfoMap.put("groupAvatar", PlaceInfo.groupInfo.getAvatar());

        // 將 participantInfoList 轉換為 List<Map<String, Object>> 並添加到 groupInfoMap 中
        List<Map<String, Object>> participantInfoListMap = new ArrayList<>();
        for (UserInfo friendInfo : PlaceInfo.participantInfoList) {
            Map<String, Object> participantInfoMap = new HashMap<>();
            participantInfoMap.put("name", friendInfo.userName);
            participantInfoMap.put("id", friendInfo.userId);
            participantInfoMap.put("avatar", friendInfo.avatar);
            participantInfoListMap.add(participantInfoMap);
        }
        costInfoMap.put("friendInfoList", participantInfoListMap);

        if(!PlaceInfo.costInfoKey.equals("")) {
            // 如果已有對應的資料，使用 updateChildren() 方法更新資料
            ref.updateChildren(costInfoMap);
        } else {
            // 如果沒有對應的資料，使用 push() 方法將新的 GroupInfo 寫入資料庫
            ref.push().setValue(costInfoMap);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        PlaceInfoInputFragment placeInfoInputFragment = (PlaceInfoInputFragment) getParentFragmentManager().findFragmentById(R.id.placeInfoInputFragment);

        //navController = Navigation.findNavController(view);
        ((Button)view.findViewById(R.id.close_btn)).setOnClickListener((View.OnClickListener)this);

        sendDataToFireBase(); // 發送消費資訊到 FireBase
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.close_btn:
                FragmentActivity homeFragmentActivity = this.getActivity();
                //getActivity().onBackPressed();

                // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                if (!isAdded()) {
                    return;
                }
                // Fragment 切換
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host, new MapFragment());
                fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
                fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                fragmentTransaction.commit();

                break;
        }
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
                fragmentTransaction.setTransition(TRANSIT_FRAGMENT_CLOSE);
                fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                fragmentTransaction.commit();
            }
        });
    }
}