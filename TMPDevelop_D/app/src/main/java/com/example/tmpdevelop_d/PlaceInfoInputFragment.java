package com.example.tmpdevelop_d;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlaceInfoInputFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextInputEditText markerNameText = null;
    private String markerNameString = "";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NavController navController;
    public FragmentManager fragmentManager;
    public FragmentTransaction fragmentTransaction;
    private ActionBar actionBar; // 使用 AndroidX

    public View view = null;

    TextView dateSelectTextView;
    Button dateSelectButton;
    TextView timeSelectTextView;
    Button timeSelectButton;
    String date;
    int hour, minute;

    public static PlaceInfoInputFragment newInstance() {
        return new PlaceInfoInputFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_place_info_input, container, false);
        // 確保 ActionBar 不為空，然後設置標題
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("輸入行程資訊");
        }
        return view;
    }

    public void onViewCreated(@NotNull View view, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //navController = Navigation.findNavController(view);
        markerNameText = (TextInputEditText) view.findViewById(R.id.input_marker_name);
        if (markerNameText != null) { // 如果使用者使用搜索地點功能添加圖釘，則此處默認自動填入獲取到的地點名稱
            markerNameText.setText(PlaceInfo.placeName);
        }

        dateSelectTextView = view.findViewById(R.id.date_select_text_view);
        dateSelectTextView.setText("yyyy/MM/dd"); // 設定默認String值
        timeSelectTextView = view.findViewById(R.id.time_select_text_view);
        timeSelectTextView.setText("HH/mm"); // 設定默認String值
        dateSelectButton = view.findViewById(R.id.date_select_button);
        // 按鈕的點擊監聽器設置
        dateSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTheme(R.style.MaterialCalendarTheme)
                        .setTitleText("日期：")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // 默認選擇當前日期
                        .build();
                //materialDatePicker.setStyle();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection));
                        dateSelectTextView.setText(date); // 將選擇的日期設置為對應 TextView 的文字
                        dateSelectTextView.setTextColor(getResources().getColor(R.color.text_black));
                    }
                });
                materialDatePicker.show(getParentFragmentManager(),"tag");
            }
        });
        timeSelectButton = view.findViewById(R.id.time_select_button);
        timeSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popTimePicker(view);
            }
        });
        ((Button)view.findViewById(R.id.next_btn)).setOnClickListener((View.OnClickListener)this);
        ((Button)view.findViewById(R.id.back_btn)).setOnClickListener((View.OnClickListener)this);
    }

    public void popTimePicker(View view) {
        int width;
        int height;

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                timeSelectTextView.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute)); // 將選擇的時間設置為對應 TextView 的文字
                timeSelectTextView.setTextColor(getResources().getColor(R.color.text_black));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),R.style.MyTimePickerDialogStyle,onTimeSetListener,hour,minute,true);
        timePickerDialog.show();
        width = (int)(getResources().getDisplayMetrics().widthPixels*0.72);
        height = (int)(getResources().getDisplayMetrics().heightPixels*0.48);
        timePickerDialog.getWindow().setLayout(width, height);
        timePickerDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.back_btn:
                FragmentActivity homeFragmentActivity = this.getActivity();
                homeFragmentActivity.onBackPressed();
                break;

            case R.id.next_btn:
                PlaceInfoInputFragment fragment = new PlaceInfoInputFragment();

                if (markerNameText != null && dateSelectTextView != null && timeSelectTextView != null) {
                    markerNameString = markerNameText.getText().toString();
                    if(!TextUtils.isEmpty(markerNameString) && dateSelectTextView.getText() != "yyyy/MM/dd" && timeSelectTextView.getText() != "HH/mm") {
                        // 若全部 TextView 不爲空，則發送數據賦值給 PlaceInfo
                        PlaceInfo.placeName = markerNameString;
                        PlaceInfo.date = date;
                        PlaceInfo.hour = hour;
                        PlaceInfo.minute = minute;

                        //navController.navigate(R.id.action_placeInfoInputFragment_to_expenseInputFragment);

                        // 使用 isAdded 方法來確定 Fragment 已經被添加到活動中，避免重複調用 commit 方法
                        if (!isAdded()) {
                            return;
                        }
                        // Fragment 切換
                        fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.nav_host, new ExpenseInputFragment());
                        fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
                        fragmentTransaction.addToBackStack(null); // 可選，將該操作添加到返回堆疊中
                        fragmentTransaction.commit();
                    } else {
                        Toast.makeText(getActivity(), (CharSequence)"請輸入完整的地點資訊", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }
}