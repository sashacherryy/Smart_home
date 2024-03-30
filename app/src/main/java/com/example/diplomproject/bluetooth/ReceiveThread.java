package com.example.diplomproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread {

    private BluetoothAdapter socket;
    private InputStream inputS;
    private OutputStream outputS;

    private byte[] rBuffer;

    public ReceiveThread(BluetoothSocket socket) {
        socket = socket;
        try {
            inputS = socket.getInputStream();
        } catch (IOException e) {
            Log.e("ReceiveThread", "Error occurred when creating input stream", e);
        }

        try {
            outputS = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("ReceiveThread", "Error occurred when creating input stream", e);
        }
    }

    @Override
    public void run() {

        rBuffer = new byte[1024];
        while (true){
            try{
                int size = inputS.read(rBuffer);
                String message = new String(rBuffer, 0, size);
                Log.d("MyLog", "Message: " + message);
            } catch (IOException e){
                break;
            }
        }
    }
    public void sendMessage(byte[] byteArray){
        try{
            outputS.write(byteArray);
        }catch (IOException e){
            Log.e("SendMessageCatch" ,"s", e);
        }
    }
}

