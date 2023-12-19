package com.ycirak.serialcomtest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ycirak.serialcomtest.databinding.ActivityMainBinding;
import  com.ycirak.serialcomtest.SerialPortUtils;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private SerialPortUtils serialPortUtils;
private ActivityMainBinding binding;
    private SerialPort serialPort;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openSerialPort();
        ToggleButton toggleButton1, toggleButton2, toggleButton3, toggleButton4;
        TextView textView1, textView2, textView3, textView4;

     binding = ActivityMainBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        toggleButton1 = findViewById(R.id.toggleButton1);
        toggleButton2 = findViewById(R.id.toggleButton2);
        toggleButton3 = findViewById(R.id.toggleButton3);


        textView4 = findViewById(R.id.textView4);



        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String hexData;
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "HydroWand Start : ", Toast.LENGTH_SHORT).show();
                    hexData = "HydroWand Start";
                } else {
                    Toast.makeText(MainActivity.this, "HydroWand Stop : ", Toast.LENGTH_SHORT).show();
                    hexData = "HydroWand Stop";
                }
                sendData(hexData);
            }
        });

        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String hexData;
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Cooling Start : ", Toast.LENGTH_SHORT).show();
                    hexData = "Cooling Start";
                } else {
                    Toast.makeText(MainActivity.this, "Cooling Stop : ", Toast.LENGTH_SHORT).show();
                    hexData = "Cooling Stop";
                }
                sendData(hexData);
            }
        });

        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String hexData;
                if (isChecked) {
                    Toast.makeText(MainActivity.this, "Oxy Start : ", Toast.LENGTH_SHORT).show();
                    hexData = "Oxy Start";
                } else {
                    Toast.makeText(MainActivity.this, "Oxy Stop : ", Toast.LENGTH_SHORT).show();
                    hexData = "Oxy Stop";
                }
                sendData(hexData);
            }
        });



    }

    /**
     * A native method that is implemented by the 'serialcomtest' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'serialcomtest' library on application startup.
    static {
        System.loadLibrary("serialcomtest");
    }

    public void sendData(String hexData) {

        serialPortUtils.sendSerialPort(hexData);

    }
    public void openSerialPort() {
        serialPortUtils = new SerialPortUtils();
        serialPortUtils.openSerialPort(     "/dev/ttyS7", "115200");
    }

    public void closeSerialPort() {
        serialPortUtils.closeSerialPort();
    }
    public void readSerialPort() {
    serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
        @Override
        public void onDataReceive(byte data, byte[] buff) {


        }
    });




    }
}