package com.example.tmpdevelop_d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    Context context;
    List<GroupInfo> groupInfoArrayList;
    List<GroupInfo> checkedGroup = new ArrayList<>();
    private int mSelectedGroup = -1; // 記錄已選中的元素位置
    private TextView mSelectedGroupText; // 顯示選中的團隊名稱的TextView

    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }

    public GroupAdapter(List<GroupInfo> groupInfoArrayList, TextView selectedGroupText) {
        this.groupInfoArrayList = groupInfoArrayList;
        this.mSelectedGroupText = selectedGroupText;

        checkedGroup.clear();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView groupNameText;
        TextView friendNameText;
        ShapeableImageView avatar;
        RadioButton radioButton;
        ItemClickListener itemClickListener;

        public MyViewHolder (View itemView) {
            super(itemView);
            groupNameText = itemView.findViewById(R.id.single_row_group_name);
            friendNameText = itemView.findViewById(R.id.single_row_group_friend_name);
            avatar = itemView.findViewById(R.id.single_row_group_avatar);
            radioButton = itemView.findViewById(R.id.single_row_group_radio_button);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_group, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        final GroupInfo groupInfo = groupInfoArrayList.get(viewHolder.getAdapterPosition());
        int groupSize = groupInfo.getFriendInfoList().size(); // 團隊的人數

        // 設置團隊名稱的 TextView
        viewHolder.groupNameText.setText(groupInfo.getGroupName()+" ("+groupSize+")");

        // 將 groupInfoArrayList 中的 friendName 提取出來存為 String
        String friendNames = "";
        for (UserInfo friend : groupInfo.getFriendInfoList()) {
            friendNames += friend.getUserName() + "; ";
        }
        viewHolder.friendNameText.setText(friendNames);

        // 設置團隊頭像
        String photoUrl = groupInfo.getAvatar();

        if (photoUrl != null) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.avatar_group)
                    .into(viewHolder.avatar);
        }

        // 設置 RadioButton 的已選中狀態
        viewHolder.radioButton.setChecked(mSelectedGroup == position); // 綁定RadioButton和數據
        viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInfo currentGroupInfo = groupInfoArrayList.get(viewHolder.getAdapterPosition());
                RadioButton radioButton = (RadioButton) v;

                if (radioButton.isChecked()) {
                    currentGroupInfo.setSelected(true);
                    checkedGroup.add(currentGroupInfo);
                }

                // 將 groupInfoArrayList 中的 friendName 提取出來存為 String
                String friendNames = "";
                for (UserInfo friend : groupInfo.getFriendInfoList()) {
                    friendNames += friend.getUserName() + "; ";
                }
                mSelectedGroup = viewHolder.getAdapterPosition(); // 更新已選中的元素位置

                mSelectedGroupText.setText(friendNames); // 更新顯示已選中的元素的TextView

                // update selected item state
                for (GroupInfo groupInfo : groupInfoArrayList) {
                    groupInfo.setSelected(false);
                }
                groupInfo.setSelected(true);
                notifyDataSetChanged(); // 更新RecyclerView
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupInfoArrayList.size();
    }

    public void setGroupInfoArrayList(ArrayList<GroupInfo> groupInfoArrayList) {
        this.groupInfoArrayList = groupInfoArrayList;
    }

    public List<GroupInfo> getGroupInfoArrayList() {
        return groupInfoArrayList;
    }
}