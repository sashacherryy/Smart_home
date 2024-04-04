package com.example.diplomproject.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diplomproject.MainActivity;
import com.example.diplomproject.adapter.BtConsts;

import java.io.IOException;

public class ConnectThread extends Thread {

    private Context context;
    private MainActivity mainActivity;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private TextView textView;
    private ReceiveThread rThread;
    private boolean isConnected = false;

    public static final String UUID = "00001101-0000-1000-8000-00805F9B34FB";

    @SuppressLint("MissingPermission")
    public ConnectThread(Context context, BluetoothAdapter btAdapter, BluetoothDevice device, TextView textView) {
        this.context = context;
        this.btAdapter = btAdapter;
        this.mDevice = device;
        this.textView = textView;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
        } catch (IOException e) {
            Log.e("ConnectThread", "Socket's create() method failed", e);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        btAdapter.cancelDiscovery();
        rThread = new ReceiveThread(mSocket);
        rThread.start();
    }


}



