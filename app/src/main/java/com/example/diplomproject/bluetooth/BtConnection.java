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
    private volatile ConnectThread connectThread; // Змінна connectThread тепер є volatile

    private TextView textView;

    public BtConnection(Context context, TextView textView) {
        this.context = context;
        this.textView = textView;
        pref = context.getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void connect() {
        String mac = pref.getString(BtConsts.MAC_KEY, "");
        if (!btAdapter.isEnabled()) {
            Log.e("BtConnection", "Bluetooth is not enabled.");
            return;
        }
        if (mac.isEmpty()) {
            Log.e("BtConnection", "MAC address is empty.");
            return;
        }
        device = btAdapter.getRemoteDevice(mac);
        if (device == null) {
            Log.e("BtConnection", "Device not found.");
            return;
        }
        ConnectThread connectThread = new ConnectThread(context, btAdapter, device, textView);
        BluetoothManager.getInstance().setConnectThread(connectThread);
        connectThread.start();
        Log.i("BtConnection", "ConnectThread started: " + connectThread);
    }


}

