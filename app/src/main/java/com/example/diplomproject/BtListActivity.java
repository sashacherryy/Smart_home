package com.example.diplomproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.diplomproject.adapter.BtAdapter;
import com.example.diplomproject.adapter.BtConsts;
import com.example.diplomproject.adapter.ListItem;
import com.example.diplomproject.bluetooth.BtConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BtListActivity extends AppCompatActivity {
    private final int BT_REQUEST_PERM = 241;
    private ArrayList<String> xmlDataList;
    private ListView listView;
    private BtAdapter adapter;
    private BluetoothAdapter btAdapter;
    private List<ListItem> list;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceArrayAdapter;
    private ArrayList<BluetoothDevice> deviceList;
    private Button btHome;
    private Context context;
    private Button searchButton;
    private ListView deviceListView;
    private BtConnection btConnection;
    private boolean isBtPermissionGranted = false;
    private TextView textView;

    private boolean exitCon = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        btConnection = new BtConnection(this , textView);
        init();
        getBtPermission();
        checkHome();
        con();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        list = new ArrayList<>();
        listView = findViewById(R.id.deviceListView);
        adapter = new BtAdapter(this, R.layout.bt_list_item, list);
        listView.setAdapter(adapter);
        search();
        getPairedDevices();
        onItemClickListener();
    }

    private void onItemClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) parent.getItemAtPosition(position);
                if (item.getItemType().equals(BtAdapter.DISCOVERY_ITEM_TYPE)) {
                    item.getBtDevice().createBond();
                } else {

                }
            }
        });
    }

    private void checkHome() {
        btHome = findViewById(R.id.btHome);
        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BtListActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void getPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            list.clear();
            for (BluetoothDevice device : pairedDevices) {
                ListItem item = new ListItem();
                item.setBtDevice(device);
                list.add(item);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void search() {
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (listView.getHeaderViewsCount() == 0) {
                    View itemView = LayoutInflater.from(BtListActivity.this).inflate(R.layout.bt_list_item_title, null);
                    listView.addHeaderView(itemView, null, false);
                }

                btAdapter.startDiscovery();

            }
        });
    }

    private void con() {
        Button btCon = findViewById(R.id.btCon);
        btCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btConnection.connect();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BT_REQUEST_PERM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isBtPermissionGranted = true;
                Toast.makeText(this, "Дозвіл отримано!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ви не недали дозволу на використання геолокації.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getBtPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, BT_REQUEST_PERM);
        } else {
            isBtPermissionGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        registerReceiver(bReciver, f1);
        registerReceiver(bReciver, f2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bReciver);
    }


    private final BroadcastReceiver bReciver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && !device.getName().isEmpty()) {
                    boolean isDeviceExist = false;
                    for (ListItem listItem : list) {
                        if (listItem.getBtDevice().getAddress().equals(device.getAddress())) {
                            isDeviceExist = true;
                            break;
                        }
                    }

                    if (!isDeviceExist) {
                        Log.d("MyLog", "MacAdress maybe of bluetooth: " + device.getAddress());
                        ListItem item = new ListItem();
                        item.setBtDevice(device);
                        item.setItemType(BtAdapter.DISCOVERY_ITEM_TYPE);
                        list.add(item);
                        adapter.notifyDataSetChanged();

                    }
                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                        if (bondState == BluetoothDevice.BOND_BONDED) {
                            exitCon = true;
                            if (exitCon) {
                                intent = new Intent(BtListActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                }

            }
        }
    };

}