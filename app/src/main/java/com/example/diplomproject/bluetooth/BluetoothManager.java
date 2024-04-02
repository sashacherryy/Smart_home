package com.example.diplomproject.bluetooth;

public class BluetoothManager {
    private static BluetoothManager instance;
    private ConnectThread connectThread;

    private BluetoothManager() {}

    public static synchronized BluetoothManager getInstance() {
        if (instance == null) {
            instance = new BluetoothManager();
        }
        return instance;
    }

    public synchronized void setConnectThread(ConnectThread thread) {
        this.connectThread = thread;
    }

    public synchronized ConnectThread getConnectThread() {
        return connectThread;
    }
}
