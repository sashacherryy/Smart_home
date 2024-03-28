package com.example.diplomproject.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.example.diplomproject.R;
import com.example.diplomproject.adapter.BtConsts;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {

    private Context context;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private TextView textView;
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
        String deviceName = mDevice.getName();
        if (deviceName == null) {
            Log.e("ConnectThread", "Device name is null");
            return;
        }

        Log.d("ConnectThread", "Attempting to connect to device: " + deviceName);
        Log.d("ConnectThread", "Attempting to connect to MAC: " + mDevice.getAddress());
        try {
            mSocket.connect();
            new ReceiveThread(mSocket).start();
            Log.d("MyLog", "Device connected");

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (textView != null) {
                        if (deviceName != null) {
                            textView.setText(deviceName);
                        } else {
                            Log.e("ConnectThread", "Device name is null");
                        }
                    }

                    TextView bluetoothNameTextView = ((Activity) context).findViewById(R.id.bluetoothsurName);
                    if (bluetoothNameTextView != null) {
                        bluetoothNameTextView.setText(deviceName);
                    } else {
                        Log.e("ConnectThread", "TextView with id 'bluetoothsurName' not found in activity_main.xml");
                    }

                    if (deviceName != null) {
                        Toast.makeText(context, "Пристрій підключено: " + deviceName, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ConnectThread", "Device name is null");
                    }
                }
            });

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(BtConsts.NAME_KEY, deviceName);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (IOException e) {
            try {
                mSocket.close();
                Log.e("ConnectThread", "Could not close the client socket", e);
                closeConnection();
            } catch (IOException closeException) {
                Log.e("ConnectThread", "Could not close the client socket", closeException);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void closeConnection() {
        try {
            mSocket.close();
            Log.d("ConnectThread", "Closed connection to device: " + mDevice.getName());
        } catch (IOException e) {
            Log.e("ConnectThread", "Could not close the client socket", e);
        }
    }
}
