package com.example.diplomproject;

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.diplomproject.bluetooth.BtConnection;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE =1;
    Button connectBlueButton;
    private String deviceName;
    BluetoothAdapter btAdapter;
    Intent btEnablingIntent;
    int requestCodeForEnable;
    private BtConnection btConnection;
    private SharedPreferences pref;
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private Runnable toggleOffRunnable;
    private Handler handler = new Handler();
    private EditText timeoutEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBlueButton = (Button) findViewById(R.id.connectBlueButton);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable=1;

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(BtConsts.NAME_KEY);

        timeoutEditText = findViewById(R.id.timeout);
        confirmButton = findViewById(R.id.confirmButton);

        checkBlue();
        init();
        confirmBut();
    }

    private void init() {

        pref = getSharedPreferences(BtConsts.MY_PREF, Context.MODE_PRIVATE);
        buttonClick();

    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView bluetoothNameTextView = findViewById(R.id.bluetoothsurName);
        if (bluetoothNameTextView != null && deviceName != null) {
            bluetoothNameTextView.setText(deviceName);
        } else {

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

                Toast.makeText(getApplicationContext(), "Bluetooth відмінений", Toast.LENGTH_LONG).show();

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

    private void checkBlue(){
        connectBlueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btAdapter == null){
                    Toast.makeText(getApplicationContext(),"Данний пристрій не підтримує Bluetooth",Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && toggleOffRunnable != null) {
            handler.removeCallbacks(toggleOffRunnable);
        }
    }

    private void confirmBut(){
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minutes = Integer.parseInt(timeoutEditText.getText().toString());

                if (minutes > 0) {
                    handler.removeCallbacksAndMessages(null);
                    startTimer(minutes);
                    toggleOffRunnable = new Runnable() {
                        @Override
                        public void run() {
                            turnOffAllToggleButtons();
                            Toast.makeText(MainActivity.this, "Всі сигнали вимкнені", Toast.LENGTH_SHORT).show();
                        }
                    };
                    handler.postDelayed(toggleOffRunnable, minutes * 60 * 1000);
                } else {
                    Toast.makeText(MainActivity.this, "Введіть додатнє число хвилин", Toast.LENGTH_SHORT).show();
                }

                timeoutEditText.setText("");

            }
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

                if (secondsLeft > 0) {
                    secondsLeft--;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void buttonClick(){

        Button voiceRecognizerButton = findViewById(R.id.VOICE_RECOGNIZER);
        voiceRecognizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startVoiceRecognition();

            }
        });

        ToggleButton heaterToggleButton = findViewById(R.id.HEATER_ON);
        heaterToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateToggleButtonStatus(buttonView, isChecked, "heater");
            }
        });

        ToggleButton diodeToggleButton = findViewById(R.id.DIODE_ON);

        diodeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateToggleButtonStatus(buttonView, isChecked, "diode");
            }
        });

        ToggleButton fanToggleButton = findViewById(R.id.FAN_ON);
        fanToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                updateToggleButtonStatus(buttonView, isChecked, "fan");

            }
        });

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

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
            }
        }, 5000);
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
        } else {

        }

    }


}
