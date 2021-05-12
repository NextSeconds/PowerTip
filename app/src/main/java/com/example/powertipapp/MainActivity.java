package com.example.powertipapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private TextView batteryTxt;
    private TextView txt4;
    private int TIP_LEVEL = 100;
    MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryTxt = (TextView) this.findViewById(R.id.textView2);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        txt4 = (TextView) this.findViewById(R.id.textView4);

        AssetFileDescriptor fileDescriptor = null;
        try {
            AssetManager a = getAssets();
            fileDescriptor = a.openFd("music/pika.mp3");
            mp.setDataSource(fileDescriptor.getFileDescriptor(),fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mp.prepare();
        } catch (IOException e) {
            Toast toast=Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();

        TIP_LEVEL = Integer.parseInt(editText.getText().toString());
        batteryTxt.setText("设置的提示电量： " + String.valueOf(TIP_LEVEL) +"%");
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);
    }

    /**
     * Handler可以用来更新UI
     * */
    @SuppressLint("HandlerLeak")
    private final Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date currentDate = new Date();
                    String displayTime1 = format1.format(currentDate);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            /**
             * 此处执行任务
             * */
            mp.start();

            mHanlder.sendEmptyMessage(1);
            mHanlder.postDelayed(this, 5 * 1000);//延迟5秒,再次执行task本身,实现了循环的效果
        }
    };
    private boolean isPlay = false;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            String tip = "";
            batteryTxt.setText("设置的提示电量： " + String.valueOf(TIP_LEVEL) +"%");

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            tip = "当前电量：" + String.valueOf(level) + "%\n";

            // Are we charging / charged?
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            tip += "当前是否充电："  + (isCharging ? "是":"否") +"\n";
            // How are we charging?
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            tip += "usb:" + (usbCharge ? "yes":"no")  +"\n";
            tip += "acCharge:" + (acCharge ? "yes":"no")  +"\n";
            txt4.setText(tip);

            if(level>=TIP_LEVEL && isCharging) {
                if(!isPlay){
                    isPlay = true;
                    mHanlder.postDelayed(task, 0);//第一次调用,延迟1秒执行task
                    Toast toast = Toast.makeText(getApplicationContext(), "电量已超过" + TIP_LEVEL + "%，开始播放提示！", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else{
                if(isPlay){
                    isPlay= false;
                    mHanlder.removeCallbacks(task);
                    if(level < TIP_LEVEL){
                        Toast toast = Toast.makeText(getApplicationContext(), "电量低于" + TIP_LEVEL + "%，停止提示！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else if(!isCharging) {
                        Toast toast = Toast.makeText(getApplicationContext(), "断开充电状态，停止提示！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else {
                        Toast toast = Toast.makeText(getApplicationContext(), "停止提示！", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        }
    };
}