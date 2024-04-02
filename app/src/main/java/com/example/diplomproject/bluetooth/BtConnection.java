package com.example.diplomproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect() {
        String mac = pref.getString(BtConsts.MAC_KEY, "");
        if (!btAdapter.isEnabled() || mac.isEmpty()) return;
        device = btAdapter.getRemoteDevice(mac);
        if (device == null) return;
        connectThread = new ConnectThread(context, btAdapter, device, textView);
        connectThread.start();
    }


}

