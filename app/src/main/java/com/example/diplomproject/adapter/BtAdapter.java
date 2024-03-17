package com.example.diplomproject.adapter;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.diplomproject.R;

import java.util.ArrayList;
import java.util.List;

public class BtAdapter extends ArrayAdapter<ListItem> {
    public static final String DEF_ITEM_TYPE = "normal";
    public static final String TITLE_ITEM_TYPE = "title";
    public static final String DISCOVERY_ITEM_TYPE = "discovery";
    private List<ListItem> mainList;
    private List<ViewHolder> listViewHolders;
    private SharedPreferences pref;

    public BtAdapter(@NonNull Context context, int resource, List<ListItem> btList) {
        super(context, resource, btList);
        mainList = btList;
        listViewHolders = new ArrayList<>();
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        switch (mainList.get(position).getItemType()) {
            case TITLE_ITEM_TYPE:
                convertView = titleItem(convertView, parent);
                break;
            default:
                convertView = defaultItem(convertView, position, parent);
                break;
        }
        return convertView;
    }

    private void savePref(int pos) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(BtConsts.MAC_KEY, mainList.get(pos).getBtDevice().getAddress());
        editor.apply();

    }

    static class ViewHolder {

        TextView tvBtName;
        CheckBox chBTSelected;

    }

    private View defaultItem(View convertView, int position, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item, null, false);
            viewHolder.tvBtName = convertView.findViewById(R.id.tvBtName);
            viewHolder.chBTSelected = convertView.findViewById(R.id.checkbox);
            convertView.setTag(viewHolder);
            listViewHolders.add(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }


        viewHolder.tvBtName.setText(mainList.get(position).getBtDevice().getName());

        if (pref.getString(BtConsts.MAC_KEY, "no bt selected").equals(mainList.get(position).getBtDevice().getAddress())) {
            viewHolder.chBTSelected.setChecked(true);
        } else {
            viewHolder.chBTSelected.setChecked(false);
        }

        viewHolder.chBTSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePref(position);

                for (ViewHolder holder : listViewHolders) {
                    holder.chBTSelected.setChecked(holder == viewHolder);
                }
            }
        });

        return convertView;
    }

    private View titleItem(View convertView, ViewGroup parent){


        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item_title, null , false);
        }
        return convertView;
    }
}