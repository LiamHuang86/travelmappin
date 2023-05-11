package com.example.tmpdevelop_d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    Context context;
    List<UserInfo> userInfoArrayList;
    List<UserInfo> checkedUser = new ArrayList<>();
    OnCheckedChangeListener listener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }

    public UserAdapter(Context context, List<UserInfo> userInfoArrayList, OnCheckedChangeListener listener) {
        this.context = context;
        this.userInfoArrayList = userInfoArrayList;
        this.listener = listener;

        checkedUser.clear();

        //userInfoArrayList.get(0).setSelected(true); // 預設使用者選項默認勾選
        //checkedUser.add(userInfoArrayList.get(0));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView userNameText;
        TextView userIdText;
        ImageView avatar;
        CheckBox checkBox;
        ItemClickListener itemClickListener;
        TextView selectedUserText; // 顯示選中的使用者名稱的TextView

        public MyViewHolder (View itemView, final OnCheckedChangeListener listener) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.single_row_user_name);
            userIdText = itemView.findViewById(R.id.single_row_user_id);
            avatar = itemView.findViewById(R.id.single_row_user_avatar);
            selectedUserText = itemView.findViewById(R.id.selected_user_text_view);
            checkBox = itemView.findViewById(R.id.single_row_user_check_box);
            checkBox.setOnClickListener(this);

            // 設置 OnCheckedChangeListener，并在回調中調用接口方法
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) {
                        listener.onCheckedChanged(getAdapterPosition(), isChecked);
                    }
                }
            });
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            this.itemClickListener.onItemClick(view,getLayoutPosition());
        }

        interface ItemClickListener {
            void onItemClick(View view, int position);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_user,null);
        MyViewHolder viewHolder = new MyViewHolder(view,listener);

        return viewHolder;
    }

    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        final UserInfo userInfo = userInfoArrayList.get(position);
        if (userInfo.isSelected()){
            checkedUser.add(userInfoArrayList.get(position));
        }

        viewHolder.checkBox.setChecked(userInfo.isSelected);
        viewHolder.checkBox.setEnabled(true);

        viewHolder.userNameText.setText(userInfo.getUserName());
        viewHolder.userIdText.setText("ID: "+userInfo.getUserId());

        // 設置參與者頭像
        String avatarUrl = userInfo.getAvatar();

        if (avatarUrl != null) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.app_icon)
                    .into(viewHolder.avatar);
        }

        viewHolder.setItemClickListener(new MyViewHolder.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CheckBox checkBox = (CheckBox) view;
                UserInfo currentUserInfo = userInfoArrayList.get(position);

                if (checkBox.isChecked()) {
                    currentUserInfo.setSelected(true);
                    checkedUser.add(currentUserInfo);
                }
                else if (!checkBox.isChecked()) {
                    currentUserInfo.setSelected(false);
                    checkedUser.remove(currentUserInfo);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return userInfoArrayList.size();
    }

    public void setUserInfoArrayList(List<UserInfo> userInfoArrayList) {
        this.userInfoArrayList = userInfoArrayList;
    }

    public List<UserInfo> getUserInfoArrayList() {
        return userInfoArrayList;
    }
}



