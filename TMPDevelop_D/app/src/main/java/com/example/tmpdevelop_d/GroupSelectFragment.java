package com.example.tmpdevelop_d;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupSelectFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView selectedGroupText;
    private String selectedGroupString = "";

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX

    public View view = null;

    GroupAdapter groupAdapter;

    List<GroupInfo> groupInfoList = new ArrayList<GroupInfo>();
    List<UserInfo> friendInfoList = new ArrayList<UserInfo>();

    // 被選擇的 groupInfo ，用來傳遞給確認行程地點資訊的頁面
    GroupInfo selectedGroupInfo;
    RadioButton radioButton;
    RecyclerView recyclerView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public GroupSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupSelectFragment newInstance(String param1, String param2) {
        GroupSelectFragment fragment = new GroupSelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("選擇團隊");
        }
        // 重新添加前，先刪除原有的動態數組，防止在 RecycleView 中重複添加
        groupInfoList.clear();
        friendInfoList.clear();
        PlaceInfo.groupInfo = null;
        PlaceInfo.friendInfoList.clear();

        // 調用從 Firestore Database 獲取全部 friendInfoList 的方法 (從資料庫獲取需要時間，若無法顯示可能需要設定執行延遲)
        getFriendInfoList();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_select, container, false);
    }

    private void getFriendInfoList() {
        String collectionName = "Users"; // 集合名稱
        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //Toast.makeText(getActivity(),"當前使用者ID: "+currentUserID, Toast.LENGTH_SHORT).show();

        db.collection(collectionName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot != null) {

                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String imageUrl = document.getString("imageUrl");
                                String userId = document.getString("uid");
                                String userName = document.getString("username");
                                UserInfo userInfo = new UserInfo(userName, userId, imageUrl);
                                friendInfoList.add(userInfo);
                            }

                            // 在此處使用 friendInfoList，例如顯示在 ListView 中
                            //Toast.makeText(getActivity(),"好友數量: "+friendInfoList.size(), Toast.LENGTH_SHORT).show();

                            // 生成對應的 groupInfoList (若無法顯示可能需要設定執行延遲)
                            setGroupInfoList();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 獲取失敗
                        Toast.makeText(getActivity(),"未能成功從雲端獲取好友列表", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setGroupInfoList() {
        CollectionReference groupsCollection = db.collection("Groups");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 獲取當前使用者id
        groupsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // 遍歷所有 group 文件的快照
                for (QueryDocumentSnapshot groupDoc : queryDocumentSnapshots) {

                    // 讀取 memberIds 數組
                    List<String> memberIds = (List<String>) groupDoc.get("memberIds");

                    // 如果 memberIds 包含當前使用者id，才處理這個群組
                    if (memberIds.contains(currentUserId)) {

                        // 讀取 groupName 和 photoUrl
                        String groupName = groupDoc.getString("groupName");
                        String avatar = groupDoc.getString("photoUrl");

                        // 創建一個臨時的 friendInfoList
                        List<UserInfo> friendInfoListTemp = new ArrayList<>();

                        // 遍歷每個 memberId
                        for (String memberId : memberIds) {

                            // 遍歷 friendInfoList，尋找符合 memberId 的 UserInfo 對象
                            for (UserInfo friendInfo : friendInfoList) {

                                // 如果找到符合的 userId，将其添加到 friendInfoListTemp 中
                                if (friendInfo.getUserId().equals(memberId)) {
                                    friendInfoListTemp.add(friendInfo);
                                    break;
                                }
                            }
                        }

                        // 創建一個新的 GroupInfo 對象，並將群組資訊添加到 groupInfoList 中
                        GroupInfo groupInfo = new GroupInfo(groupName, friendInfoListTemp, avatar);
                        groupInfoList.add(groupInfo);
                    }
                }

                // 成功獲取到所有符合條件的 groupInfoList ，實例化 RecycleView
                initRecycleView();
            }
        });
    }

    private void initRecycleView() { // 設置 RecycleView 的 Adapter
        groupAdapter = new GroupAdapter(groupInfoList, selectedGroupText);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(groupAdapter);
        //Toast.makeText(getActivity(),"設置成功", Toast.LENGTH_SHORT).show();
    }

    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //navController = Navigation.findNavController(view);
        selectedGroupText = (TextView) view.findViewById(R.id.selected_group_text_view);
        radioButton = (RadioButton) view.findViewById(R.id.single_row_group_radio_button);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_group_select);

        selectedGroupInfo = null; // 重新進入頁面時，清空之前選擇的朋友名單
        PlaceInfo.groupInfo = null; // 重新進入頁面時，清空 PlaceInfo 的選擇的朋友名單

        // 兩個按鈕的點擊監聽器
        ((Button)view.findViewById(R.id.next_btn)).setOnClickListener((View.OnClickListener)this);
        ((Button)view.findViewById(R.id.cancel_btn)).setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.cancel_btn:
                FragmentActivity homeFragmentActivity = this.getActivity();
                homeFragmentActivity.onBackPressed();
                break;

            case R.id.next_btn:
                if (selectedGroupText != null) {
                    selectedGroupString = selectedGroupText.getText().toString();
                    if(!TextUtils.isEmpty(selectedGroupString)) { // 如文本不爲空，則發送數據
                        updateSelectedGroupInfo();
                        PlaceInfo.groupInfo = selectedGroupInfo;
                        PlaceInfo.friendInfoList = selectedGroupInfo.getFriendInfoList();

                        //navController.navigate(R.id.action_groupSelectFragment_to_placeInfoInputFragment);

                        // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                        if (!isAdded()) {
                            return;
                        }
                        // Fragment 切換
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host, new PlaceInfoInputFragment());
                        fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
                        fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                        fragmentTransaction.commit();
                    } else {
                        Toast.makeText(getActivity(),"請選擇至少一個團隊", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    // 在向 PlaceInfo 的 userInfoList 添加 selectedUserInfoArrayList 前，添加所有已選中的 UserInfo
    public void updateSelectedGroupInfo() {
        for (int i = 0; i < groupAdapter.checkedGroup.size(); i++) {
            GroupInfo groupInfo = groupAdapter.checkedGroup.get(i);
            if (groupInfo.isSelected) {
                selectedGroupInfo = new GroupInfo(groupInfo.getGroupName(),groupInfo.getFriendInfoList(),groupInfo.getAvatar());
            }
        }
    }
}