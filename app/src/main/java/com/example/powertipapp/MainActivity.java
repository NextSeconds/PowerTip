package com.example.powertipapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements VolumeChangeObserver.VolumeChangeListener  {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final int IMPORT_FILE_CODE = 1;
    private ProgressBar progressBarVolume,progressBarPower;
    private TextView tishiyinliang1,dangqiandianliang1,textViewTip, textViewFilePath;
    private int TIP_LEVEL = 100;//电量提示
    private int VOLUME_PROGRESS = 100;

    private VolumeChangeObserver mVolumeChangeObserver;
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //实例化对象并设置监听器
        mVolumeChangeObserver = new VolumeChangeObserver(this);
        mVolumeChangeObserver.setVolumeChangeListener(this);
        int initVolume = mVolumeChangeObserver.getCurrentMusicVolume();
        ShowTip("当前音量" + initVolume);
        bindViews();

        //设置当前音乐
        try {
            AssetManager a = getAssets();
            AssetFileDescriptor fileDescriptor = a.openFd(String.valueOf(textViewFilePath.getText()));
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            ShowTip(e.toString());
            e.printStackTrace();
        }
    }
    //初始化组件绑定关系 同时进行首次组件数据更新
    @SuppressLint("SetTextI18n")
    private void bindViews() {
        textViewTip = this.findViewById(R.id.textViewTip);
        //提示电量显示
        SeekBar seekBarPowerTip = this.findViewById(R.id.seekBarPowerTip);
        TextView tishidianliang1 = this.findViewById(R.id.tishidianliang1);
        seekBarPowerTip.setProgress(TIP_LEVEL);
        tishidianliang1.setText(seekBarPowerTip.getProgress() + "%");
        //注册提示音量拖拽事件
        seekBarPowerTip.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tishidianliang1.setText(seekBarPowerTip.getProgress() + "%");
                TIP_LEVEL = seekBarPowerTip.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //音量显示
        progressBarVolume = this.findViewById(R.id.progressBarVolume);
        tishiyinliang1 = this.findViewById(R.id.tishiyinliang1);

        UpdateVolume(mVolumeChangeObserver.getCurrentMusicVolume());
        //当前电量显示
        progressBarPower = this.findViewById(R.id.progressBarPower);
        dangqiandianliang1 = this.findViewById(R.id.dangqiandianliang1);

        //注册文件选择按钮事件
        Button btn = findViewById(R.id.btnSelect);
        textViewFilePath = findViewById(R.id.textViewFilePath);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*"); //选择音频
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
    }

    //Handler可以用来更新UI
    @SuppressLint("HandlerLeak")
    private final Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /*switch (msg.what) {
                case 1:
                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date currentDate = new Date();
                    String displayTime1 = format1.format(currentDate);
                    break;
                default:
                    break;
            }*/
            super.handleMessage(msg);
        }
    };

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            //此处执行任务
            mediaPlayer.start();
            mHanlder.sendEmptyMessage(1);
            mHanlder.postDelayed(this, 5 * 1000);//延迟5秒,再次执行task本身,实现了循环的效果
        }
    };

    private boolean isPlay = false;
    //电量改变事件
    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            String tip = "";
            //显示当前电量
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            progressBarPower.setProgress(level);
            dangqiandianliang1.setText(level + "%");

            // Are we charging / charged?
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            tip += "当前是否充电：" + (isCharging ? "是" : "否") + "\n";
            // How are we charging?
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            tip += "usb:" + (usbCharge ? "yes" : "no") + "\n";
            tip += "acCharge:" + (acCharge ? "yes" : "no") + "\n";
            textViewTip.setText(tip);

            if (level >= TIP_LEVEL && isCharging) {
                if (!isPlay) {
                    isPlay = true;
                    mHanlder.postDelayed(task, 0);//第一次调用,延迟1秒执行task
                    ShowTip("电量已超过" + TIP_LEVEL + "%，开始播放提示！");
                }
            } else {
                if (isPlay) {
                    isPlay = false;
                    mHanlder.removeCallbacks(task);//停止task的执行 如正在执行，则停止下次执行
                    if (level < TIP_LEVEL) {
                        ShowTip("电量低于" + TIP_LEVEL + "%，停止提示！");
                    } else if (!isCharging) {
                        ShowTip("断开充电状态，停止提示！");
                    } else {
                        ShowTip("停止提示！");
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        //注册广播接收器
        mVolumeChangeObserver.registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //解注册广播接收器
        mVolumeChangeObserver.unregisterReceiver();
        super.onPause();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVolumeChanged(int volume) {
        //系统媒体音量改变时的回调
        UpdateVolume(volume);
    }

    private void UpdateVolume(int volume) {
        VOLUME_PROGRESS = volume * 100 / 15;
        progressBarVolume.setProgress(VOLUME_PROGRESS);
        tishiyinliang1.setText(VOLUME_PROGRESS + "%");
    }

    //弹出界面提示
    private void ShowTip(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
        System.out.println("【输出】" + msg);
    }

    String path;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                textViewFilePath.setText(path);
            }
            else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPathFromUri(this, uri);
                textViewFilePath.setText(path);
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                textViewFilePath.setText(path);
            }
            //设置当前音乐
            ShowTip(path);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    @SuppressLint("NewApi")
    public static String getPathFromUri(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }
        // 判斷是否為Android 4.4之後的版本
        final boolean after44 = Build.VERSION.SDK_INT >= 19;
        if (after44 && DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是Android 4.4之後的版本，而且屬於文件URI
            final String authority = uri.getAuthority();
            // 判斷Authority是否為本地端檔案所使用的
            if ("com.android.externalstorage.documents".equals(authority)) {
                // 外部儲存空間
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                String path;
                if ("primary".equals(type)) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                } else {
                    path = "/storage/".concat(type).concat("/").concat(divide[1]);
                }
                return path;
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                // 下載目錄
                final String docId = DocumentsContract.getDocumentId(uri);
                if (docId.startsWith("raw:")) {
                    final String path = docId.replaceFirst("raw:", "");
                    return path;
                }
                final Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                String path = queryAbsolutePath(context, downloadUri);
                return path;
            } else if ("com.android.providers.media.documents".equals(authority)) {
                // 圖片、影音檔案
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                Uri mediaUri = null;
                if ("image".equals(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                String path = queryAbsolutePath(context, mediaUri);
                return path;
            }
        } else {
            // 如果是一般的URI
            final String scheme = uri.getScheme();
            String path = null;
            if ("content".equals(scheme)) {
                // 內容URI
                path = queryAbsolutePath(context, uri);
            } else if ("file".equals(scheme)) {
                // 檔案URI
                path = uri.getPath();
            }
            return path;
        }
        return null;
    }

    public static String queryAbsolutePath(final Context context, final Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                return cursor.getString(index);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_FILE_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String path = UriUtil.getPath(MainActivity.this, uri);
        }
    }

    public String getPath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}