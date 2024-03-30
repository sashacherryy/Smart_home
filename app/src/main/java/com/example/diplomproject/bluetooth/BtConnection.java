package com.example.diplomproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import com.example.diplomproject.adapter.BtConsts;

public class BtConnection {

    private Context context;
    private SharedPreferences pref;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private ConnectThread connectThread;
    private TextView textView;

    public BtConnection(Context context, TextView textView) {

        this.context = context;
        this.textView = textView;
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        Log.d("btAdapter" , "btAdapter " + btAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("btAdapter" , "btAdapter " + btAdapter);

    }

    public void connect(){

        String mac = pref.getString(BtConsts.MAC_KEY, "");
        if (!btAdapter.isEnabled() || mac.isEmpty()) return;
        device = btAdapter.getRemoteDevice(mac);
        Log.e("MyLogDevice", "Device" + device );
        if (device == null) return;
        connectThread = new ConnectThread(context, btAdapter, device, textView);
        connectThread.start();

    }

    public void sendMessage(String message){
        connectThread.getRThread().sendMessage(message.getBytes());
    }

}
