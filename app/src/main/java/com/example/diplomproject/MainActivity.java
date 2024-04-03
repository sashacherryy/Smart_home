package com.example.diplomproject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diplomproject.adapter.BtConsts;
import com.example.diplomproject.bluetooth.BluetoothManager;
import com.example.diplomproject.bluetooth.BtConnection;
import com.example.diplomproject.bluetooth.ConnectThread;
import com.example.diplomproject.bluetooth.ReceiveThread;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE = 1;

    private String deviceName;
    private BluetoothAdapter btAdapter;
    private volatile ConnectThread connectThread;
    private Intent btEnablingIntent;
    private int requestCodeForEnable;
    private BtConnection btConnection;
    private SharedPreferences pref;
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Runnable toggleOffRunnable;
    private BluetoothSocket mSocket;
    private Handler handler = new Handler();
    private EditText timeoutEditText;
    private Button confirmButton, buttonA , connectBlueButton;
    private TextView textView;
    private ReceiveThread rThread;
    private String deviceMAC;
    private OutputStream outputS;
    private ProgressDialog progressDialog;
    private boolean isCon = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btConnection = new BtConnection(this, textView);
        connectBlueButton = findViewById(R.id.connectBlueButton);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(BtConsts.NAME_KEY);
        deviceMAC = intent.getStringExtra("deviceMAC");
        Log.i("DeviceMac", "DevcieMac" + deviceMAC);
        rThread = intent.getParcelableExtra("rThread");
        if (rThread == null) {
            Log.e("MainActivity", "rThread is null");
        }

        timeoutEditText = findViewById(R.id.timeout);
        confirmButton = findViewById(R.id.confirmButton);

        checkBlue();
        init();
        confirmBut();

        Boolean isConnected = getIntent().getBooleanExtra("isConnected", false);
        if(isConnected) {
            ToggleButton toggleButtonA = findViewById(R.id.FAN_ON);
            toggleButtonA.setOnCheckedChangeListener((buttonView, isChecked) -> sendData(isChecked ? "A" : "a"));

            ToggleButton toggleButtonB = findViewById(R.id.HEATER_ON);
            toggleButtonB.setOnCheckedChangeListener((buttonView, isChecked) -> sendData(isChecked ? "B" : "b"));

            ToggleButton toggleButtonC = findViewById(R.id.DIODE_ON);
            toggleButtonC.setOnCheckedChangeListener((buttonView, isChecked) -> sendData(isChecked ? "C" : "c"));

            showProgressDialog();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        connectToBluetoothDevice(deviceMAC);
                        setTextView(deviceName);
                    }
                }
            }, 2000);
        }


        Log.i("MainActivity", "Connected: " + isConnected);
        Log.e("MainActivity", "btConnection" + btConnection);
    }

    private void init() {
        pref = getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        buttonClick();
    }
    protected void setTextView(String str) {
        TextView bluetoothNameTextView = findViewById(R.id.bluetoothsurName);
        if (bluetoothNameTextView != null && str != null && isCon) {
            bluetoothNameTextView.setText(str);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == requestCodeForEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth увімкнений", Toast.LENGTH_LONG).show();
                updateBluetoothNameTextView();
                Intent i = new Intent(MainActivity.this, BtListActivity.class);
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth вимкнений", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                TextView textView = findViewById(R.id.textView);
                textView.setText(recognizedText);
                handleVoiceCommand(recognizedText);
                clearTextViewWithAnimation();
            }
        }
    }

    private void updateBluetoothNameTextView() {
        String bluetoothName = pref.getString(BtConsts.NAME_KEY, "");
        TextView bluetoothNameTextView = findViewById(R.id.bluetoothsurName);
        if (bluetoothNameTextView != null) {
            bluetoothNameTextView.setText(bluetoothName);
        } else {
            Log.e("MainActivity", "TextView with id 'bluetoothsurName' not found");
        }
    }

    private void checkBlue() {
        connectBlueButton.setOnClickListener(v -> {
            if (btAdapter == null) {
                Toast.makeText(getApplicationContext(), "Даний пристрій не підтримує Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                if (!btAdapter.isEnabled()) {
                    startActivityForResult(btEnablingIntent, requestCodeForEnable);
                } else {
                    Intent i = new Intent(MainActivity.this, BtListActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void confirmBut() {
        confirmButton.setOnClickListener(v -> {
            String minutesStr = timeoutEditText.getText().toString();
            if (!minutesStr.isEmpty()) {
                int minutes = Integer.parseInt(minutesStr);
                if (minutes > 0) {
                    handler.removeCallbacksAndMessages(null);
                    startTimer(minutes);
                    toggleOffRunnable = () -> {
                        turnOffAllToggleButtons();
                        Toast.makeText(MainActivity.this, "Всі сигнали вимкнені", Toast.LENGTH_SHORT).show();
                    };
                    handler.postDelayed(toggleOffRunnable, minutes * 60 * 1000);
                } else {
                    Toast.makeText(MainActivity.this, "Введіть додатнє число хвилин", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Поле введення порожнє", Toast.LENGTH_SHORT).show();
            }
            timeoutEditText.setText("");
        });
    }

    private void turnOffAllToggleButtons() {
        ToggleButton heaterToggleButton = findViewById(R.id.HEATER_ON);
        ToggleButton diodeToggleButton = findViewById(R.id.DIODE_ON);
        ToggleButton fanToggleButton = findViewById(R.id.FAN_ON);

        heaterToggleButton.setChecked(false);
        diodeToggleButton.setChecked(false);
        fanToggleButton.setChecked(false);

        TextView textView = findViewById(R.id.textView);
        textView.setText("Усі сигнали вимкнені");
        clearTextViewWithAnimation();
    }

    private void startTimer(int minutes) {
        TextView timeLess = findViewById(R.id.timeLess);
        handler.post(new Runnable() {
            int secondsLeft = minutes * 60;

            @Override
            public void run() {
                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;
                String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
                timeLess.setText(timeLeftFormatted);
                sendData(timeLeftFormatted);

                if (secondsLeft > 0) {
                    secondsLeft--;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void buttonClick() {
        Button voiceRecognizerButton = findViewById(R.id.VOICE_RECOGNIZER);
        voiceRecognizerButton.setOnClickListener(v -> startVoiceRecognition());

        ToggleButton heaterToggleButton = findViewById(R.id.HEATER_ON);
        heaterToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateToggleButtonStatus(buttonView, isChecked, "heater"));

        ToggleButton diodeToggleButton = findViewById(R.id.DIODE_ON);
        diodeToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateToggleButtonStatus(buttonView, isChecked, "diode"));

        ToggleButton fanToggleButton = findViewById(R.id.FAN_ON);
        fanToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> updateToggleButtonStatus(buttonView, isChecked, "fan"));
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "uk-UA"); // Встановлення мови розпізнавання (українська)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говоріть щось..."); // Повідомлення для користувача
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Розпізнавання голосу не підтримується на цьому пристрої", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateToggleButtonStatus(CompoundButton buttonView, boolean isChecked, String deviceName) {

        String status = isChecked ? deviceName + "_on" : deviceName + "_off";
        int statusResourceId = getResources().getIdentifier(status, "string", getPackageName());
        String statusText = getResources().getString(statusResourceId);

        //TextView textView = findViewById(R.id.textView);
        //textView.setText(statusText);

    }

    @SuppressLint("MissingPermission")
    public void connectToBluetoothDevice(String deviceMAC) {
        if(!deviceMAC.isEmpty()) {
            BluetoothDevice device = btAdapter.getRemoteDevice(deviceMAC);
            Log.e("ConnectToBluetoothDevice" , "ConnectToBluetoothDevice підтримується" + deviceMAC);
            try {
                mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")); // UUID для з'єднання з Bluetooth пристроєм
                mSocket.connect();
                outputS = mSocket.getOutputStream();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (deviceName != null) {
                        Toast.makeText(this, "Пристрій підключено: " + deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
                isCon = true;
                if(outputS != null) dismissProgressDialog();
                Log.d("BluetoothApp", "Connected to Bluetooth device: " + device.getName());
            } catch (IOException e) {
                Log.e("BluetoothApp", "Error connecting to Bluetooth device", e);
            }
        }else{
            Log.e("ConnectToBluetoothDevice" , "ConnectToBluetoothDevice не підтримується");
        }
    }

    private void clearTextViewWithAnimation() {
        TextView textView = findViewById(R.id.textView);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(3500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        textView.startAnimation(fadeOut);

        handler.postDelayed(() -> textView.setVisibility(View.VISIBLE), 5000);
    }

    private void handleVoiceCommand(String command) {
        ToggleButton diodeToggleButton = findViewById(R.id.DIODE_ON);
        ToggleButton fanToggleButton = findViewById(R.id.FAN_ON);
        ToggleButton heaterToggleButton = findViewById(R.id.HEATER_ON);

        String lowerCaseCommand = command.toLowerCase();

        if (lowerCaseCommand.equals("увімкни світло")) {
            diodeToggleButton.setChecked(true);
        } else if (lowerCaseCommand.equals("вимкни світло")) {
            diodeToggleButton.setChecked(false);
        } else if (lowerCaseCommand.equals("увімкни вентилятор")) {
            fanToggleButton.setChecked(true);
        } else if (lowerCaseCommand.equals("вимкни вентилятор")) {
            fanToggleButton.setChecked(false);
        } else if (lowerCaseCommand.equals("увімкни нагрівач")) {
            heaterToggleButton.setChecked(true);
        } else if (lowerCaseCommand.equals("вимкни нагрівач")) {
            heaterToggleButton.setChecked(false);
        }
    }
    private void sendData(String data) {
        if (mSocket == null || outputS == null) {
            Toast.makeText(this, "Bluetooth з'єднання не встановлено", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            outputS.write(data.getBytes());
            Log.d("BluetoothApp", "Data sent: " + data);
        } catch (IOException e) {
            Log.e("BluetoothApp", "Error sending data", e);
        }
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connecting...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}

