package com.example.bleota;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
@SuppressLint("MissingPermission")
public class ota extends AppCompatActivity {
    private static final String TAG = "OTA";
    private TextView tv_name; // 声明一个文本视图对象
    private TextView tv_address; // 声明一个文本视图对象
    private TextView tv_status; // 声明一个文本视图对象
    private TextView tv_file; // 声明一个文本视图对象
    private int file_length = 0;
    FileInputStream fis;
    ParcelFileDescriptor file;
    private Button btn_connect; // 声明一个按钮对象
    private Button btn_file; // 声明一个按钮对象
    private Button btn_send; // 声明一个按钮对象
    private UUID write_UUID_service; // 写的服务编号
    private UUID write_UUID_chara; // 写的特征编号
    private BluetoothGatt mBluetoothGatt; // 声明一个蓝牙GATT客户端对象
    private BluetoothDevice mRemoteDevice; // 声明一个蓝牙设备对象
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota);
        initView(); // 初始化视图
        initBluetooth(); // 初始化蓝牙适配器
    }
    @SuppressLint("SetTextI18n")
    private void initBluetooth() {
        BluetoothAdapter mBluetoothAdapter; // 声明一个蓝牙适配器对象
        // 获取蓝牙管理器，并从中得到蓝牙适配器
        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bm.getAdapter(); // 获取蓝牙适配器
        String address = getIntent().getStringExtra("address");
        // 根据设备地址获得远端的蓝牙设备对象
        mRemoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        tv_name.setText("设备名称："+mRemoteDevice.getName());
        tv_address.setText("设备MAC："+mRemoteDevice.getAddress());
        tv_status.setText("连接状态：未连接");
    }
    private void initView() {
        tv_name = findViewById(R.id.tv_name);
        tv_address = findViewById(R.id.tv_address);
        tv_status = findViewById(R.id.tv_status);
        btn_file=findViewById(R.id.btn_file);
        btn_send=findViewById(R.id.btn_send);
        btn_connect = findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(v -> {
            // 连接GATT服务器
            mBluetoothGatt = mRemoteDevice.connectGatt(this, false, mGattCallback);
        });
        btn_send.setOnClickListener(v -> {
            // 连接GATT服务器
            sendCommand(0x08);
        });
        btn_file.setOnClickListener(v -> {
            // 连接GATT服务器
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //startActivity(intent);
            startActivityForResult(intent,1240);
        });
        btn_send.setEnabled(false);
        btn_file.setEnabled(false);
    }
    @Override
    protected  void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        tv_file = (TextView)findViewById(R.id.tv_file);
        if (resultCode == Activity.RESULT_OK && requestCode == 1240) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null, null);

            try {
                // moveToFirst() returns false if the cursor has 0 rows. Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor != null && cursor.moveToFirst()) {

                    // Note it's called "Display Name". This is
                    // provider-specific, and might not necessarily be the file name.
                    @SuppressLint("Range") String displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    Log.i(TAG, "Display Name: " + displayName);

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                    String size = null;
                    if (!cursor.isNull(sizeIndex)) {

                        size = cursor.getString(sizeIndex);
                    } else {
                        size = "Unknown";
                    }
                    Log.i(TAG, "Size: " + size);

                    tv_file.setText("File Name:" + displayName +'\n' +"Size: " +  size + "Byte");
                }
            } finally {
                cursor.close();
            }

            try{
                file = getContentResolver().openFileDescriptor(uri, "r");

                fis = new FileInputStream(file.getFileDescriptor());
                file_length = fis.available();
            }catch (IOException e){
                e.printStackTrace();
            }
            btn_send.setEnabled(true);
        }
    }

    // 向智能小车发送指令
    private void sendCommand(int command) {
        new Thread(() -> writeCommand((byte) command)).start();
    }
    private void writeCommand(byte command) {
        // 拿到写的特征值
        BluetoothGattCharacteristic chara = mBluetoothGatt.getService(write_UUID_service)
                .getCharacteristic(write_UUID_chara);
        Log.d(TAG, "writeCharacteristic "+command);
        chara.setValue(new byte[]{command}); // 设置写特征值
        mBluetoothGatt.writeCharacteristic(chara); // 往GATT服务器写入特征值
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        // BLE连接的状态发生变化时回调
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange status=" + status + ", newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) { // 连接成功
                gatt.discoverServices(); // 开始查找GATT服务器提供的服务
                runOnUiThread(() -> {
                    tv_status.setText("已连接");
                    btn_connect.setVisibility(View.GONE);
                    btn_file.setEnabled(true);
                });
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 连接断开
                btn_send.setEnabled(false);
                mBluetoothGatt.close(); // 关闭GATT客户端
            }
        }
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered status"+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取GATT服务器提供的服务列表
                List<BluetoothGattService> gattServiceList= mBluetoothGatt.getServices();
                for (BluetoothGattService gattService : gattServiceList) {
                    List<BluetoothGattCharacteristic> charaList = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic chara : charaList) {
                        int charaProp = chara.getProperties(); // 获取该特征的属性
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                            write_UUID_chara = chara.getUuid();
                            write_UUID_service = gattService.getUuid();
                            Log.d(TAG, "no_response write_chara=" + write_UUID_chara + ", write_service=" + write_UUID_service);
                        }
                    }
                }
            } else {
                Log.d(TAG, "onServicesDiscovered fail-->" + status);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect(); // 断开GATT连接
        }
    }
}
