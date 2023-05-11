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
 * Use the {@link ParticipantSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParticipantSelectFragment extends Fragment implements View.OnClickListener,
        ListAdapter.CheckboxCheckedListener,
        UserAdapter.OnCheckedChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView selectedParticipantText;
    private String selectedParticipantString = "";

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX

    public View view = null;

    UserAdapter userAdapter;

    // 團隊内部的所有成員
    List<UserInfo> friendInfoList = new ArrayList<UserInfo>();

    // 被選擇的朋友所構成的 ArrayList，也就是參與消費的朋友
    List<UserInfo> participantInfoList = new ArrayList<UserInfo>();

    public ParticipantSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ParticipantSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParticipantSelectFragment newInstance(String param1, String param2) {
        ParticipantSelectFragment fragment = new ParticipantSelectFragment();
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
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("選擇參與消費的成員");
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_participant_select, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //navController = Navigation.findNavController(view);
        selectedParticipantText = (TextView) view.findViewById(R.id.selected_participant_text_view);

        // 重新添加前，先刪除原有的動態數組
        PlaceInfo.participantInfoList.clear();

        friendInfoList = PlaceInfo.friendInfoList;

        // 設置RecycleView的Adapter
        userAdapter = new UserAdapter(getContext(), friendInfoList, this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_participant_select);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(userAdapter);

        // 對已選擇的朋友清單TextView進行點擊事件的設置，點擊則更新顯示當前選擇的朋友的TextView(測試用，非必要)
        selectedParticipantText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTextView();
            }
        });

        participantInfoList.clear(); // 清空之前選擇的朋友名單

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

                if (selectedParticipantText != null) {
                    selectedParticipantString = selectedParticipantText.getText().toString();
                    if(!TextUtils.isEmpty(selectedParticipantString)) { //如文字不爲空，則發送數據
                        updateSelectedUserInfoArrayList();
                        PlaceInfo.participantInfoList.addAll(participantInfoList);

                        //navController.navigate(R.id.action_participantSelectFragment_to_payerSelectFragment);

                        // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                        if (!isAdded()) {
                            return;
                        }
                        // Fragment 切換
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host, new PayerSelectFragment());
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
        for (int i = 0; i < userAdapter.checkedUser.size(); i++) {
            UserInfo userInfo = userAdapter.checkedUser.get(i);
            if (userInfo.isSelected) {
                participantInfoList.add(new UserInfo(userInfo.getUserName(),userInfo.getUserId(),userInfo.getAvatar()));
            }
        }
    }

    public void updateTextView() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;

        // 檢測checkedUser是否為空，若為空則提前結束方法，防止程式崩潰
        if (userAdapter.checkedUser == null || userAdapter.checkedUser.size() == 0) {
            Toast.makeText(getActivity(),"請選擇至少一位使用者",Toast.LENGTH_SHORT).show();
            selectedParticipantText.setText("");
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
            selectedParticipantText.setText(stringBuilder.toString());
        }
    }
}