package com.example.tmpdevelop_d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {
    Context context;

    List<String> userNameList;
    List<String> userIdList;
    List<Integer> avatarList; // 儲存圖像的resourceID
    CheckboxCheckedListener checkedListener;

        public ListAdapter(@NonNull Context context, List<String> userNameList, List<String> userIdList, List<Integer> avatarList) {
        super(context, R.layout.single_row_user, R.id.single_row_group_name);
        this.context = context;
        this.userNameList = userNameList;
        this.userIdList = userIdList;
        this.avatarList = avatarList;
    }

    public class MyViewHolder {
        ImageView imageView;
        TextView textView01;
        TextView textView02;
        CheckBox checkBox;

        MyViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.single_row_group_avatar);
            textView01 = (TextView) view.findViewById(R.id.single_row_group_name);
            textView02 = (TextView) view.findViewById(R.id.single_row_group_friend_name);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        MyViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_row_user,parent,false);
            holder = new MyViewHolder (row);
            row.setTag(holder);
        } else {
            holder = (MyViewHolder) row.getTag();
        }

        holder.imageView.setImageResource(avatarList.get(position));
        holder.textView01.setText(userNameList.get(position));
        holder.textView02.setText(userIdList.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkedListener != null) {
                    checkedListener.getCheckboxCheckedListener(position);
                }
            }
        });

        return super.getView(position, convertView, parent);
    }

    public interface CheckboxCheckedListener {
        void getCheckboxCheckedListener(int position);
    }

    public void setCheckboxCheckedListener (CheckboxCheckedListener checkedListener) {
        this.checkedListener = checkedListener;
    }

}
