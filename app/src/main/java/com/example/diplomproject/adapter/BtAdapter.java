package com.example.diplomproject.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.diplomproject.R;

import java.util.ArrayList;
import java.util.List;

public class BtAdapter  extends ArrayAdapter<ListItem> {
    private  List<ListItem> mainList;
    private  List<ViewHolder> listViewHolders;
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
        ViewHolder viewHolder;

        if (convertView == null){

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bt_list_item, null , false);
            viewHolder.tvBtName = convertView.findViewById(R.id.tvBtName);
            viewHolder.chBTSelected= convertView.findViewById(R.id.checkbox);
            convertView.setTag(viewHolder);
            listViewHolders.add(viewHolder);

        } else {

            viewHolder= (ViewHolder) convertView.getTag();

        }

        viewHolder.tvBtName.setText(mainList.get(position).getBtName());
        viewHolder.chBTSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(ViewHolder holder: listViewHolders){
                    holder.chBTSelected.setChecked(false);

                }
                viewHolder.chBTSelected.setChecked(true );
                savePref(position);

            }
        });
        if(pref.getString(BtConsts.MAC_KEY, "no bt selected").equals(mainList.get(position).getBtMac()))viewHolder.chBTSelected.setChecked(true);

        return convertView;
    }

    private void savePref(int pos){

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(BtConsts.MAC_KEY, mainList.get(pos).getBtMac());
        editor.apply();

    }

    static class ViewHolder{

        TextView tvBtName;
        CheckBox chBTSelected;

    }


}