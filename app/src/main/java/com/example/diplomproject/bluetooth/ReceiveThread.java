package com.example.diplomproject.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveThread extends Thread {

    private BluetoothSocket socket;
    private InputStream inputS;
    private OutputStream outputS;

    private byte[] rBuffer;
    public String message;

    public ReceiveThread(BluetoothSocket socket) {
        this.socket = socket;
        try {
            inputS = socket.getInputStream();
        } catch (IOException e) {
            Log.e("ReceiveThread", "Error occurred when creating input stream", e);
        }

    }
    @Override
    public void run() {
        rBuffer = new byte[2048];
        while (true){
            try{
                int size = inputS.read(rBuffer);
                message = new String(rBuffer, 0, size);
                Log.d("ReceiveThread", "Message: " + message);

            } catch (IOException e){
                break;
            }
        }
    }

    public String getMessage() {
        return message;
    }
}
