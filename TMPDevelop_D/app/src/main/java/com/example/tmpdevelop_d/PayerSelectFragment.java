package com.example.tmpdevelop_d;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PayerSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PayerSelectFragment extends Fragment implements View.OnClickListener,
        ListAdapter.CheckboxCheckedListener,
        UserAdapter.OnCheckedChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView selectedUserText;
    private String selectedUserString = "";

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX

    public View view = null;

    UserAdapter userAdapter;
    List<UserInfo> friendInfoList = new ArrayList<UserInfo>();
    // 被選擇的 userInfo 所構成的 ArrayList，用來傳遞給確認行程地點資訊的頁面
    List<UserInfo> selectedUserInfoList = new ArrayList<UserInfo>();

    public PayerSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendNameInputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PayerSelectFragment newInstance(String param1, String param2) {
        PayerSelectFragment fragment = new PayerSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_payer_select, container, false);
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("選擇付款者");
        }
        return view;
    }

    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //navController = Navigation.findNavController(view);
        selectedUserText = (TextView) view.findViewById(R.id.selected_user_text_view);

        // 重新添加前，先刪除原有的動態數組
        friendInfoList.clear();
        PlaceInfo.payer = null;

        friendInfoList = PlaceInfo.participantInfoList;

        // 設置RecycleView的Adapter
        userAdapter = new UserAdapter(getContext(), friendInfoList, this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_user_select);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(userAdapter);

        // 對已選擇的朋友清單TextView進行點擊事件的設置，點擊則更新顯示當前選擇的朋友的TextView(測試用，非必要)
        selectedUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTextView();
            }
        });

        //ListAdapter listAdapter = new ListAdapter(getContext(),userNameList,userIdList,avatarList);
        //recyclerView.setAdapter(listAdapter);
        //listAdapter.setCheckboxCheckedListener(this);
        selectedUserInfoList.clear(); // 清空之前選擇的朋友名單
        //PlaceInfo.userInfoList.clear(); // 清空 PlaceInfo 的選擇的朋友名單
        //PlaceInfo.friendNameList.add("預設使用者"); // 默認將userName添加為第一條使用者名稱
        //friendNameList.add("預設使用者");

        // 兩個按鈕的點擊監聽器
        ((Button)view.findViewById(R.id.next_btn)).setOnClickListener((View.OnClickListener)this);
        ((Button)view.findViewById(R.id.back_btn)).setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.back_btn:
                FragmentActivity homeFragmentActivity = this.getActivity();
                homeFragmentActivity.onBackPressed();

                break;

            case R.id.next_btn:

                if (selectedUserText != null) {
                    selectedUserString = selectedUserText.getText().toString();
                    if(!TextUtils.isEmpty(selectedUserString)) { //如文字不爲空，則發送數據
                        updateSelectedUserInfoArrayList();
                        PlaceInfo.payer = selectedUserInfoList.get(0);
                        PlaceInfo.isComplete = true;
                        //navController.navigate(R.id.action_payerSelectFragment_to_confirmationFragment);

                        // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                        if (!isAdded()) {
                            return;
                        }
                        // Fragment 切換
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host, new ConfirmationFragment());
                        fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
                        fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                        fragmentTransaction.commit();
                    } else {
                        Toast.makeText(getActivity(),"請選擇至少一位付款者", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    public void getCheckboxCheckedListener(int position) {
        //Toast.makeText(getActivity(),"測試", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(int position, boolean isChecked) {
        if (isChecked) {
            ////Toast.makeText(getActivity(),"已選擇: "+friendInfoList.get(position).getUserName(), Toast.LENGTH_SHORT).show();
            // 更新顯示當前選擇的朋友的TextView
            // 在選擇或取消選擇 CheckBox 時，RecyclerView 中的項目會重新繪製，此過程中可能會出現一些延遲
            // 如果在這段延遲期間調用 updateTextView()，則 TextView 的文本可能不會立即更新，因此在更新前添加一些延遲
            new Handler().postDelayed(() -> updateTextView(), 10);
        } else {
            ////Toast.makeText(getActivity(),"已取消選擇: "+friendInfoList.get(position).getUserName(), Toast.LENGTH_SHORT).show();
            // 更新顯示當前選擇的朋友的TextView
            // 在選擇或取消選擇 CheckBox 時，RecyclerView 中的項目會重新繪製，此過程中可能會出現一些延遲
            // 如果在這段延遲期間調用 updateTextView()，則 TextView 的文本可能不會立即更新，因此在更新前添加一些延遲
            new Handler().postDelayed(() -> updateTextView(), 10);
        }
    }
    // 在向 PlaceInfo 的 userInfoList 添加 selectedUserInfoArrayList 前，添加所有已選中的 UserInfo
    public void updateSelectedUserInfoArrayList() {
        //selectedUserInfoArrayList.add(
        for (int i = 0; i < userAdapter.checkedUser.size(); i++) {
            UserInfo userInfo = userAdapter.checkedUser.get(i);
            if (userInfo.isSelected) {
                selectedUserInfoList.add(new UserInfo(userInfo.getUserName(),userInfo.getUserId(),userInfo.getAvatar()));
            }
        }
    }

    public void updateTextView() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;

        // 檢測checkedUser是否為空，若為空則提前結束方法，防止程式崩潰
        if (userAdapter.checkedUser == null || userAdapter.checkedUser.size() == 0) {
            Toast.makeText(getActivity(),"請選擇至少一位使用者",Toast.LENGTH_SHORT).show();
            //selectedUserText.setText(null);
            selectedUserText.setText("");
            return;
        }

        do {
            UserInfo userInfo = userAdapter.checkedUser.get(i);
            stringBuilder.append(userInfo.getUserName());
            if (i != userAdapter.checkedUser.size() - 1) {
                stringBuilder.append("; ");
            }
            i++;
        } while (i < userAdapter.checkedUser.size());

        if (userAdapter.checkedUser.size() > 0) {
            selectedUserText.setText(stringBuilder.toString());
        }
    }
}