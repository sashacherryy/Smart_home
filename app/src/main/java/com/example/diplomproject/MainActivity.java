package com.example.diplomproject;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diplomproject.adapter.BtConsts;


public class MainActivity extends AppCompatActivity {

    Button connectBlueButton;
    BluetoothAdapter btAdapter;
    Intent btEnablingIntent;
    int requestCodeForEnable;
    private SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBlueButton = (Button) findViewById(R.id.connectBlueButton);

        btEnablingIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable=1;

        checkBlue();
        init();
    }

    private void init() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pref = getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        Log.d("MyLog", "BT NAME : " + pref.getString(BtConsts.MAC_KEY, "no bt selected"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeForEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is Enable", Toast.LENGTH_LONG).show();
                Intent i = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void checkBlue(){
        connectBlueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btAdapter == null){
                    Toast.makeText(getApplicationContext(),"Bluetooth doesn't support on this device",Toast.LENGTH_LONG).show();
                }else{
                    if(!btAdapter.isEnabled()){
                        startActivityForResult(btEnablingIntent,requestCodeForEnable);
                    }else{
                        Intent i = new Intent(MainActivity.this, BtListActivity.class);
                        startActivity(i);
                    }
                }
            }
        });

    }




}