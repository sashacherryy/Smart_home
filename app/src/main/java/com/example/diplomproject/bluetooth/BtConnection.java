package com.example.diplomproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diplomproject.adapter.BtConsts;

import java.io.IOException;

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

    public void sendMessage(String message) {
        if (connectThread != null ) {
            connectThread.getRThread().sendMessage(message.getBytes());
        } else {
            Log.e("BtConnection", "Unable to send message: Connection not established or still initializing");
        }
    }
}

