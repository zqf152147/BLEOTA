package com.example.bleota;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.bleota.ex.BlueListAdapter;
import com.example.bleota.ex.BlueDevice;
import com.example.bleota.ex.BluetoothUtil;
import com.example.bleota.ex.PermissionUtil;
@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private static final String TAG = "MainActivity";
    private CheckBox ck_bluetooth; // 声明一个复选框对象
    private TextView tv_discovery; // 声明一个文本视图对象
<<<<<<< HEAD
=======
//    private ListView lv_bluetooth; // 声明一个用于展示蓝牙设备的列表视图对象
>>>>>>> 2c80663a2e953efa963967140084c20a9f3e8b7e
    private BlueListAdapter mListAdapter; // 声明一个蓝牙设备的列表适配器对象

    private final Map<String, BlueDevice>  mDeviceMap = new HashMap<>(); // 蓝牙设备映射
    private final List<BlueDevice> mDeviceList = new ArrayList<>(); // 蓝牙设备列表
    private final Handler mHandler = new Handler(Looper.myLooper()); // 声明一个处理器对象
    private BluetoothAdapter mBluetoothAdapter; // 声明一个蓝牙适配器对象
    private boolean isScaning = false; // 是否正在扫描

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
<<<<<<< HEAD
        setContentView(R.layout.main);
=======
        setContentView(R.layout.activity_scan_car);
>>>>>>> 2c80663a2e953efa963967140084c20a9f3e8b7e
        initView(); // 初始化视图
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android12之后使用蓝牙需要蓝牙连接权限
            if (PermissionUtil.checkPermission(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT}, 1)) {
<<<<<<< HEAD
                //startActivity(new Intent(this, ota.class));
=======
                startActivity(new Intent(this, ota.class));
>>>>>>> 2c80663a2e953efa963967140084c20a9f3e8b7e
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android6.0之后使用蓝牙需要定位权限
            if (PermissionUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, 1)) {
<<<<<<< HEAD
            }
        } else {

=======
                //startActivity(new Intent(this, ScanCarActivity.class));
            }
        } else {
            startActivity(new Intent(this, ota.class));
>>>>>>> 2c80663a2e953efa963967140084c20a9f3e8b7e
        }
        initBluetooth();
        if (BluetoothUtil.getBlueToothStatus()) { // 已经打开蓝牙
            ck_bluetooth.setChecked(true);
        }
    }

    private void initView() {
        ListView lv_bluetooth;
        ck_bluetooth = findViewById(R.id.ck_bluetooth);
        tv_discovery = findViewById(R.id.tv_discovery);
        lv_bluetooth = findViewById(R.id.lv_bluetooth);
        ck_bluetooth.setOnCheckedChangeListener(this);
        mListAdapter = new BlueListAdapter(this, mDeviceList);
        lv_bluetooth.setAdapter(mListAdapter);
        lv_bluetooth.setOnItemClickListener((parent, view, position, id) -> {
            BlueDevice item = mDeviceList.get(position);
            Intent intent = new Intent(this, ota.class);
            intent.putExtra("address", item.address);
            startActivity(intent);
        }
        );
    }
    public  void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.ck_bluetooth) {
            if (isChecked) { // 开启蓝牙功能
                ck_bluetooth.setText("蓝牙开");
                if (!BluetoothUtil.getBlueToothStatus()) { // 还未打开蓝牙
                    BluetoothUtil.setBlueToothStatus(true); // 开启蓝牙功能
                }
                mHandler.post(mScanStart); // 启动开始BLE扫描的任务
            } else { // 关闭蓝牙功能
                ck_bluetooth.setText("蓝牙关");
                mHandler.removeCallbacks(mScanStart); // 移除开始BLE扫描的任务
                BluetoothUtil.setBlueToothStatus(false); // 关闭蓝牙功能
                mDeviceList.clear();
                mListAdapter.notifyDataSetChanged();
            }
        }
    }
    private void initBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "当前设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
            finish(); // 关闭当前页面
        }
        // 获取蓝牙管理器，并从中得到蓝牙适配器
        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bm.getAdapter(); // 获取蓝牙适配器
    }
    private final Runnable mScanStart = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (!isScaning && BluetoothUtil.getBlueToothStatus()) {
                isScaning = true;
                // 获取BLE设备扫描器
                BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
                scanner.startScan(mScanCallback); // 开始扫描BLE设备
                tv_discovery.setText("点击BLE设备进入管理界面");
            } else {
                mHandler.postDelayed(this, 2000);
            }
        }
    };
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (TextUtils.isEmpty(result.getDevice().getName())) {
                return;
            }
            Log.d(TAG, "callbackType=" + callbackType + ", result=" + result);
            // 下面把找到的蓝牙设备添加到设备映射和设备列表
            BlueDevice device = new BlueDevice(result.getDevice().getName(), result.getDevice().getAddress(), 0);
            mDeviceMap.put(device.address, device);
            mDeviceList.clear();
            mDeviceList.addAll(mDeviceMap.values());
            runOnUiThread(() -> mListAdapter.notifyDataSetChanged());
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
    private final Runnable mScanStop = () -> {
        isScaning = false;
        // 获取BLE设备扫描器
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(mScanCallback); // 停止扫描BLE设备
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mScanStart); // 移除开始BLE扫描的任务
        mHandler.removeCallbacks(mScanStop); // 移除停止BLE扫描的任务
    }
}