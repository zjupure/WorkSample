package com.zlc.work.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * author: liuchun
 * date: 2018/7/31
 */
public class DeviceUtil {
    // 非法错误的Mac地址列表
    private static final List<String> FAIL_MAC = new ArrayList<>();

    static {
        FAIL_MAC.add("02:00:00:00:00:00");
        FAIL_MAC.add("00:00:00:00:00:00");
        FAIL_MAC.add("0");
    }

    /**
     * 获取设备IMEI号
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(@NonNull Context context) {
        String imei = "";
        boolean hasPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
        if (hasPhonePermission) {
            try {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    // Android O上优先获取IMEI号，修复小米6, MIUI8主卡槽插了电信卡获取deviceID为MEID的问题
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        imei = tm.getImei();
                    }
                    // 其他版本使用getDeviceId获取
                    if (TextUtils.isEmpty(imei)) {
                        imei = tm.getDeviceId();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // NULL值转为"", avoid NPE
        if (TextUtils.isEmpty(imei)) {
            imei = "";
        }

        return imei;
    }

    /**
     * 获取设备Mac Address
     */
    @SuppressLint("MissingPermission")
    public static String getMacAddr(@NonNull Context context) {
        String macAddr = "";
        boolean hasMacPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED;
        if (hasMacPermission) {
            try {
                WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    WifiInfo info = wm.getConnectionInfo();
                    if (info != null) {
                        macAddr = info.getMacAddress();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 修复Android M上时候系统API无法获取Mac Address的问题
        if (TextUtils.isEmpty(macAddr) || FAIL_MAC.contains(macAddr)) {
            macAddr = getMacAddrByNetConfig();
        }
        // NULL值转换为"", avoid NPE
        if (TextUtils.isEmpty(macAddr)) {
            macAddr = "";
        }

        return macAddr;
    }

    /**
     * 通过读取配置文件获取Mac地址
     */
    private static String getMacAddrByNetConfig() {
        String macAddr = "";
        try {
            macAddr = getMacAddrByInterfaceName("wlan0");
            if (TextUtils.isEmpty(macAddr)) {
                macAddr = getMacAddrByInterfaceName("eth0");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAddr;
    }

    /**
     * 通过读取配置文件获取Mac地址
     */
    private static String getMacAddrByInterfaceName(String interfaceName) throws SocketException {
        if (TextUtils.isEmpty(interfaceName)) {
            return "";
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface mNetWorkInterface = interfaces.nextElement();
            String mInterfaceName = mNetWorkInterface.getName();
            if (TextUtils.isEmpty(mInterfaceName) || !mInterfaceName.equals(interfaceName)) {
                continue;
            }

            byte[] addr = mNetWorkInterface.getHardwareAddress();
            if (addr == null || addr.length == 0) {
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            return buf.toString();
        }
        return "";
    }

    /**
     * 获取设备Android ID
     */
    public static String getAndroidId(@NonNull Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // NULL转换成"", avoid NPE
        if (TextUtils.isEmpty(androidId)) {
            androidId = "";
        }
        return androidId;
    }

    /**
     *  获取设备串号
     */
    @SuppressLint("MissingPermission")
    public static String getBuildSerial(@NonNull Context context) {
        String serial = "";
        try {
            boolean hasPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasPhonePermission) {
                serial = Build.getSerial();
                if (TextUtils.isEmpty(serial)) {
                    serial = Build.SERIAL;
                }
            }  else {
                serial = Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // NULL转换成"", avoid NPE
        if (TextUtils.isEmpty(serial)) {
            serial = "";
        }

        return serial;
    }

    /**
     * 获取蓝牙地址
     */
    @SuppressLint("MissingPermission")
    public static String getBluetoothAddresss(@NonNull Context context) {
        String blueMacAddr = "";
        boolean hasBluetootchPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED;
        if (hasBluetootchPermission) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                blueMacAddr = adapter.getAddress();
            }

            if (TextUtils.isEmpty(blueMacAddr) || FAIL_MAC.contains(blueMacAddr)) {
                blueMacAddr = getBluetoothAddressByReflect(context);
            }
        }
        // NULL转换成"", avoid NPE
        if (TextUtils.isEmpty(blueMacAddr)) {
            blueMacAddr = "";
        }
        return blueMacAddr;
    }

    /**
     * 通过反射方式获取蓝牙地址
     */
    private static String getBluetoothAddressByReflect(@NonNull Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            try {
                Field serviceField = adapter.getClass().getDeclaredField("mService");
                serviceField.setAccessible(true);
                Object manager = serviceField.get(adapter);

                if (manager != null) {
                    Method method = manager.getClass().getDeclaredMethod("getAddress");
                    return (String) method.invoke(manager);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取当前进程名称
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (ActivityManager.RunningAppProcessInfo process : am.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    return process.processName;
                }
            }
        } catch (Exception e) {
            // ActivityManager.getRunningAppProcesses() may throw NPE in some custom-made devices (oem BIRD)
        }

        //try to read process name in /proc/pid/cmdline if no result from activity manager
        String cmdline = null;
        BufferedReader bufferReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(String.format("/proc/%d/cmdline", pid));
            bufferReader = new BufferedReader(fileReader);
            cmdline = bufferReader.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(bufferReader);
            IoUtil.close(fileReader);
        }
        return cmdline;
    }

    /**
     * 检查设备是否Root过
     */
    public static boolean isDeviceRooted() {
        String[] suDirs = new String[]{"/system/bin/", "/system/xbin/", "/system/sbin", "/sbin/", "/vendor/bin/",
                "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/"};
        // 搜索su文件是否存在
        for (String suDir : suDirs) {
            File file = new File(suDir + "su");
            if (file.exists()) {
                return true;
            }
        }
        // 检测Superuser.apk是否存在
        File file = new File("/system/app/Superuser.apk");
        return file.exists();
    }
}
