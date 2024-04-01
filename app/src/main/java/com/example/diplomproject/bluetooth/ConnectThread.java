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

import androidx.appcompat.app.AppCompatActivity;

import com.example.diplomproject.MainActivity;
import com.example.diplomproject.R;
import com.example.diplomproject.adapter.BtConsts;

import java.io.IOException;

public class ConnectThread extends Thread {

    private Context context;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private TextView textView;
    private ReceiveThread rThread;
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
        String deviceName;
        deviceName = mDevice.getName();
        btAdapter.cancelDiscovery();
        try {
            mSocket.connect();
            rThread = new ReceiveThread(mSocket);
            rThread.start();
            new Handler(Looper.getMainLooper()).post(() -> {

                TextView bluetoothNameTextView = ((AppCompatActivity) context).findViewById(R.id.bluetoothsurName);
                if (bluetoothNameTextView != null) {
                    bluetoothNameTextView.setText(deviceName);
                }

                if (deviceName != null) {
                    Toast.makeText(context, "Пристрій підключено: " + deviceName, Toast.LENGTH_SHORT).show();
                }
            });

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(BtConsts.NAME_KEY, deviceName);
            context.startActivity(intent);

        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            closeConnection();
        }
    }

    @SuppressLint("MissingPermission")
    public void closeConnection() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e("ConnectThread", "Could not close the client socket", e);
        }
    }

    public ReceiveThread getRThread() {
        return rThread;
    }



}