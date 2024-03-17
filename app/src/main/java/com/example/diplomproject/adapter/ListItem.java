package com.example.diplomproject.adapter;

import android.bluetooth.BluetoothDevice;

public class ListItem {
    private BluetoothDevice btDevice;
    public String itemType = BtAdapter.DEF_ITEM_TYPE;

    public BluetoothDevice getBtDevice() {
        return btDevice;
    }

    public void setBtDevice(BluetoothDevice btDevice) {
        this.btDevice = btDevice;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}