package com.example.tmpdevelop_d;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExpenseInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseInputFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextInputEditText itemNameTextInput;
    private TextInputEditText expenseTextInput;
    private String itemNameString = "";
    private String expenseString = "";
    private int expense;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX

    public View view = null;

    public ExpenseInputFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ExpenseInputFragment newInstance(String param1, String param2) {
        ExpenseInputFragment fragment = new ExpenseInputFragment();
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
        view = inflater.inflate(R.layout.fragment_expense_input, container, false);
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("輸入行程消費");
        }
        return view;
    }

    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //navController = Navigation.findNavController(view);
        itemNameTextInput = view.findViewById(R.id.input_item_name);
        expenseTextInput = view.findViewById(R.id.input_expense);
        expenseString = expenseTextInput.getText().toString();

        ((Button)view.findViewById(R.id.confirm_btn)).setOnClickListener((View.OnClickListener)this);
        ((Button)view.findViewById(R.id.back_btn)).setOnClickListener((View.OnClickListener)this);
    }

    public void onClick(View view) {

        switch(view.getId()) {

            case R.id.back_btn:
                FragmentActivity homeFragmentActivity = this.getActivity();
                homeFragmentActivity.onBackPressed();

                break;

            case R.id.confirm_btn:

                if (itemNameTextInput != null && expenseTextInput != null) {
                    itemNameString = itemNameTextInput.getText().toString();
                    expenseString = expenseTextInput.getText().toString();

                    if(!TextUtils.isEmpty(itemNameString)) {
                        if (!TextUtils.isEmpty(expenseString)) {
                            try { // 如文字不爲空，且數字格式正確，則獲得 int expense 並發送數據
                                expense = Integer.parseInt(expenseString);
                                PlaceInfo.itemName = itemNameString;
                                PlaceInfo.expense = expense;
                                //Toast.makeText(getActivity(),"消費項目: "+PlaceInfo.itemName+"\n"+"消費金額: "+PlaceInfo.expense,Toast.LENGTH_SHORT).show();
                                //navController.navigate(R.id.action_expenseInputFragment_to_participantSelectFragment);

                                // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                                if (!isAdded()) {
                                    return;
                                }
                                // Fragment 切換
                                fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.nav_host, new ParticipantSelectFragment());
                                fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
                                fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                                fragmentTransaction.commit();
                            } catch (NumberFormatException e) {
                                // 處理數字格式錯誤異常
                                Toast.makeText(getActivity(),"數字格式錯誤，請確認輸入的内容", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(),"請輸入消費金額", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(),"請輸入消費項目", Toast.LENGTH_SHORT).show();
                    }
                }
        }

    }

}