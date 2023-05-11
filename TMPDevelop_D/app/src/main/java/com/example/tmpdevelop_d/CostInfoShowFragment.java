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
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CostInfoShowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CostInfoShowFragment extends Fragment implements View.OnClickListener {

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX
    public View view = null;
    private TextView textView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("CostInfo");

    public CostInfoShowFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CostInfoShowFragment newInstance(String param1, String param2) {
        CostInfoShowFragment fragment = new CostInfoShowFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("顯示已儲存的消費");
        }
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

        view = inflater.inflate(R.layout.fragment_cost_info_show, container, false);
        textView = view.findViewById(R.id.confirmation_message);
        textView.setText("已找到當前地點的消費資訊"+"\n"+"\n"
                +"圖釘地點序號: "+markerIndex+"\n"
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
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        // navController = Navigation.findNavController(view);
        ((Button)view.findViewById(R.id.close_btn)).setOnClickListener((View.OnClickListener)this);
    }

    @Override
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