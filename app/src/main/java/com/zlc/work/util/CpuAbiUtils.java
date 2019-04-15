package com.zlc.work.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CpuAbiUtils {
    private static final String TAG = "CpuAbiUtils";
    /* apk 中 lib 目录的前缀标示。比如 lib/armeabi/libshare_v2.so */
    private static final String APK_LIB_DIR_PREFIX = "lib/";
    /* libs目录so后缀 */
    private static final String APK_LIB_SUFFIX = ".so";
    /* 当前手机指令集 */
    private static String currentInstructionSet = null;
    /* 当前APP运行的主abi */
    private static String primaryCpuAbi = null;

    /**
     * 获取当前运行的指令集
     */
    public static String getCurrentInstructionSet() {
        if (currentInstructionSet != null) {
            return currentInstructionSet;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0以上VMRuntime才有这个方法
            try {
                Class<?> clazz = Class.forName("dalvik.system.VMRuntime");
                Method currentGet = clazz.getDeclaredMethod("getCurrentInstructionSet");
                currentGet.setAccessible(true);

                currentInstructionSet = (String) currentGet.invoke(null);
                return currentInstructionSet;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 4.x只支持arm指令集
            currentInstructionSet = "arm";
        }
        //默认返回arm指令集
        return "arm";
    }

    /**
     * 获取设备支持的abi列表
     */
    public static String[] getSupportAbis() {
        String[] cpuAbis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cpuAbis = Build.SUPPORTED_ABIS;
        } else {
            cpuAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        return cpuAbis;
    }

    /**
     * 获取设备运行的cpuAbi模式
     */
    public static String getPrimaryCpuAbi(Context context) {
        if (primaryCpuAbi != null) {
            return primaryCpuAbi;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0以上Application才有primaryCpuAbi字段
            try {
                ApplicationInfo appInfo = context.getApplicationInfo();
                Field abiField = ApplicationInfo.class.getDeclaredField("primaryCpuAbi");
                abiField.setAccessible(true);
                primaryCpuAbi = (String)abiField.get(appInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (TextUtils.isEmpty(primaryCpuAbi)) {
            primaryCpuAbi = findMatchedAbi(context);
        }

        return primaryCpuAbi;
    }

    /**
     * 根据宿主APK内放置的libs文件夹类型和
     * 设备支持的abi列表选择最match的abi
     */
    private static String findMatchedAbi(Context context) {
        String matchedAbi = null;
        String[] cpuAbis = getSupportAbis();
        boolean[] abiResolved = new boolean[cpuAbis.length];
        String apkPath = context.getApplicationInfo().sourceDir;
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(APK_LIB_DIR_PREFIX)
                        || !name.endsWith(APK_LIB_SUFFIX)) {
                    continue;
                }
                String[] splits = name.split("/");
                if (splits.length < 3) {
                    continue;
                }
                // entry: lib/armeabi/xxx.so
                String arch = splits[1];
                for (int index = 0; index < cpuAbis.length; index++) {
                    if (TextUtils.equals(cpuAbis[index], arch)) {
                        // 对应arch目录下存在so库
                        abiResolved[index] = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(zipFile);
        }

        for (int index = 0; index < cpuAbis.length; index++) {
            if (abiResolved[index]) {
                // 对应的abi目录下找到了so库
                matchedAbi = cpuAbis[index];
                break;
            }
        }
        // 宿主没有放置lib目录，则按默认的模式运行
        if (TextUtils.isEmpty(matchedAbi)) {
            matchedAbi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                    ? Build.SUPPORTED_ABIS[0] : Build.CPU_ABI;
        }
        return matchedAbi;
    }

    /**
     * 获取与primaryCpuAbi能够兼容的abi
     * 比如32位：armeabi与armeabi-v7a是兼容的
     */
    public static String getCompatCpuAbi(String primaryCpuAbi) {
        if (TextUtils.equals(primaryCpuAbi, "armeabi")
                && isCompatible("armeabi-v7a")) {
            // 主abi是armeabi，如果设备支持armeabi-v7a，兼容模式
            return "armeabi-v7a";
        }

        if (TextUtils.equals(primaryCpuAbi, "armeabi-v7a")
                && isCompatible("armeabi")) {
            // 主abi是armeabi-v7a，如果设备支持armeabi，兼容模式
            return "armeabi";
        }
        return "";
    }

    /**
     * 指定的cpuArch是否与设备兼容
     */
    private static boolean isCompatible(String cpuArch) {
        String[] cpuAbis = getSupportAbis();
        for (String cpuAbi : cpuAbis) {
            if (TextUtils.equals(cpuAbi, cpuArch)) {
                return true;
            }
        }
        return false;
    }
}
