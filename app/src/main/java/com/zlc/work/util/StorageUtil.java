package com.zlc.work.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * author: liuchun
 * date: 2018/7/31
 */
public class StorageUtil {
    private static final String TAG = "StorageUtils";
    /**
     * 存储卡的类型
     */
    public static final int INTERNAL_STORAGE = 0;  // 内置存储卡
    public static final int EXTERNAL_SDCARD = 1;  // 外置SD卡
    public static final int EXTERNAL_USB = 2;          // 外接USB存储
    public static final int MEDIA_UNKNOWN = 3;     // 未知设备
    /* 指纹文件名前缀 */
    private static final String FINGER_NAME_PREFIX = ".finger_";  // 每次动态生成+时间戳
    // 内置存储卡，不可移除的分区
    private static StorageItem mBuiltInStorage = null;

    /**
     * 获取内部/data分区
     */
    public static StorageItem getInternalStorage(Context context) {

        File internalFile = context.getFilesDir().getParentFile();
        StorageItem item = new StorageItem(internalFile.getAbsolutePath());

        return item;
    }

    /**
     * 获取不可移除的SD卡分区
     */
    public static StorageItem getUnRemovableStorage(Context context) {
        if (mBuiltInStorage != null) {
            return mBuiltInStorage;
        }

        List<StorageItem> storageItems = getStorageList(context);
        for (StorageItem item : storageItems) {
            if (!item.mRemovable) {
                mBuiltInStorage = item;
                return item;
            }
        }
        return null;
    }

    /**
     * 获取可用的存储信息，包括SdCard或Usb存储
     */
    public static List<StorageItem> getStorageList(Context context) {
        // 首先尝试通过反射StorageManager类获取SDCard列表
        List<StorageItem> arrayList = getStorageListByReflection(context);

        if (arrayList == null || arrayList.size() <= 0) {
            // 反射获取失败或低于4.0的设备，尝试通过读取mount表获取
            arrayList = getStorageListByMountFile(context);
        }

        //按照内置卡在前，外置卡在后的顺序排列
        List<StorageItem> results = new ArrayList<>();
        for (StorageItem item : arrayList) {

            if (!item.isRemovable()) {
                results.add(0, item);
            } else {
                results.add(item);
            }
        }

        return results;
    }

    /**
     * 通过反射{@link android.os.storage.StorageManager}和{@link android.os.storage.StorageVolume}的隐藏API
     * 获取SdCard相关信息
     * <p>
     * Note: Android N上{@link android.os.storage.StorageVolume}类不再是hide的了
     */
    private static List<StorageItem> getStorageListByReflection(Context context) {
        ArrayList<StorageItem> arrayList = new ArrayList<>();

        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        // Android 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            List<StorageVolume> storageVolumes = sm.getStorageVolumes();

            for (StorageVolume storageVolume : storageVolumes) {
                StorageItem info = new StorageItem(storageVolume, context);

                if (Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context)) {
                    arrayList.add(info);
                }
            }

            return arrayList;
        }

        // Android 4.0-6.0
        try {
            Method a = sm.getClass().getMethod("getVolumeList");
            a.setAccessible(true);
            // 反射StorageManager类的方法getVolumeList()
            Object[] storageVolumes = (Object[]) a.invoke(sm);

            for (Object storageVolume : storageVolumes) {
                StorageItem info = new StorageItem(storageVolume, context);
                //判断挂载状态和读写性
                if (Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context)) {
                    arrayList.add(info);
                }
            }
        } catch (Exception e) {
            /* ignore */
        }

        // 反射StorageVolume失败，则通过反射StorageManager的方法获取
        if (arrayList.size() <= 0) {
            try {
                Method b = sm.getClass().getMethod("getVolumePaths");
                b.setAccessible(true);
                //Method c = sm.getClass().getMethod("getVolumeState", String.class);
                //c.setAccessible(true);

                String[] paths = (String[]) b.invoke(sm);  // 调用getVolumePaths
                for (String path : paths) {
                    if (!checkPathValidate(path)) {
                        continue;  // 校验路径的有效性
                    }

                    StorageItem info = new StorageItem(path, context);
                    if (Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context)) {
                        arrayList.add(info);
                    }
                }
            } catch (Exception e) {
                /* ignore */
            }
        }

        return arrayList;
    }

    /**
     * 通过读取/proc/mounts文件或执行mount命令获取
     * 并对结果进行过滤，筛选出有效的存储路径
     * /system/etc/vold.fstab文件是预设信息，无法区分挂载状态，需要重新设定
     */
    private static List<StorageItem> getStorageListByMountFile(Context context) {
        // 读取/proc/mounts文件或执行mount命令
        ArrayList<String> mMounts = new ArrayList<>();
        // 读取/system/etc/vold.fstab无法区分挂载状态
        ArrayList<String> mVold = new ArrayList<>();
        // 挂载点与设备名映射表
        HashMap<String, String> mMountDevs = new HashMap<>();

        //添加默认的路径/mnt/sdcard
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");

        /* 尝试读取/proc/mounts文件  */
        BufferedReader bufferedReader = null;
        try {
            File mountFile = new File("/proc/mounts");
            if (mountFile.exists() && mountFile.canRead()) {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(mountFile)));  // 读取/proc/mounts文件
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("mount").getInputStream())); // 执行linux mount命令
            }

            while (true) {
                String line = bufferedReader.readLine();

                if (line == null) {
                    break;  //读到文件的末尾
                }

                if (line.contains("secure") || line.contains("asec") || line.contains("firmware") || line.contains("obb") || line.contains("tmpfs")) {
                    // 需要排除的关键词，不是sdcard挂载点
                    continue;
                }

                String[] split = line.split("\\s+");  //空格分割
                if (split == null || split.length <= 4) {
                    continue;  // /proc/mounts文件一行一般为6个
                }

                // <dev> <mount_point> <filesystem> <rw,useid,groudid> <0> <0>
                String dev = split[0];          //设备名
                String mount_point = split[1];  // 挂载点

                String tmp = mount_point.toLowerCase();
                if (dev.contains("/dev/block/vold/") || dev.contains("/dev/block/sd") || dev.contains("/dev/sd") || dev.contains("/dev/fuse") || dev.contains("/dev/lefuse")) {
                    // 常见设备名
                    if (!tmp.equals("/mnt/sdcard")) {
                        mMounts.add(mount_point);  //添加可能的挂载点
                    }

                    mMountDevs.put(mount_point, dev);
                } else if (tmp.contains("emmc") || tmp.contains("storage") || tmp.contains("sdcard") || tmp.contains("external") || tmp.contains("ext_sd")
                        || tmp.contains("ext_card") || tmp.contains("extsdcard") || tmp.contains("external_sd") || tmp.contains("emulated")
                        || tmp.contains("/mnt/media_rw/") || tmp.contains("flash")) {
                    // 常见挂载点
                    if (!tmp.equals("/mnt/sdcard")) {
                        mMounts.add(mount_point);  //添加可能的挂载点
                    }
                    mMountDevs.put(mount_point, dev);
                }
            }
        } catch (Exception e) {
            /* ignore */
        } finally {
            IoUtil.close(bufferedReader);
        }

        /* 尝试读取/system/etc/vold.fstab */
        try {
            File voldFile = new File("/system/etc/vold.fstab");
            if (!voldFile.exists()) {
                voldFile = new File("/etc/vold.fstab");  //有些手机存放位置不一致
            }

            if (voldFile.exists()) {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(voldFile)));

                while (true) {
                    String line = bufferedReader.readLine();

                    if (line == null) {
                        break;   // 读到文件的末尾
                    }

                    // vold.fstab文件都是以dev_mount开头
                    // Format: dev_mount <label> <mount_point> <part> <sysfs_path1...>
                    if (line.startsWith("dev_mount")) {
                        String[] splits = line.split("\\s+");  //空格分割
                        String mount_point = splits[2];

                        if (mount_point.contains(":")) {
                            mount_point = mount_point.substring(0, mount_point.indexOf(":"));
                        }

                        if (!mount_point.equals("/mnt/sdcard")) {
                            mVold.add(mount_point);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(bufferedReader);
        }

        ArrayList<String> mList = new ArrayList<>(mMounts);
        //合并两个文件的内容
        for (String vold : mVold) {
            if (!mMounts.contains(vold)) {
                mList.add(vold);
            }
        }
        mVold.clear();

        /**
         * 对读取的结果进行过滤，分为三个阶段
         * 1. 文件路径的标准化，很多路径可能是软连接，如/mnt/sdcard--->/storage/emulated/0
         * 2. 根据/Android/data/{package_name}/files目录读写性过滤
         * 3. 同一设备挂载到多个目录，需要过滤，如/dev/fuse-->/mnt/shell/emulated,/storage/emulated/0
         */
        // step 1, 标准化路径
        ArrayList<String> tmp1 = new ArrayList<>();
        for (String mount : mList) {
            if (!checkPathValidate(mount)) {
                continue;  //无效路径, pass
            }

            String realPath = mount;
            try {
                File file = new File(mount);
                realPath = file.getCanonicalPath();
            } catch (IOException e) {
                /* ingore */
            }

            // 不是重复路径就添加到结果集合
            if (!tmp1.contains(realPath)) {
                tmp1.add(realPath);
            }
        }

        // step 2, 根据读写性进行筛选过滤
        ArrayList<StorageItem> tmp2 = new ArrayList<>();
        for (String mount : tmp1) {
            StorageItem info = new StorageItem(mount, context);

            if (Environment.MEDIA_MOUNTED.equals(info.mState) && info.canWrite(context)) {
                //
                tmp2.add(info);
            }
        }

        // step3, 解决同一设备挂载到多个目录现象, 通过创建指纹文件识别
        ArrayList<StorageItem> tmp3 = new ArrayList<>();
        String fingerName = FINGER_NAME_PREFIX + System.currentTimeMillis();   //指纹文件名
        for (StorageItem mount : tmp2) {

            if (mount.mPath.contains("legacy")) {
                continue;   // 向下兼容的挂载目录,需要过滤掉
            }

            if (hasFingerPrint(mount, context, fingerName)) {
                continue;   // 存在指纹文件，说明是重复的目录
            }

            if (!createFingerPrint(mount, context, fingerName)) {
                continue;   // 创建指纹文件失败，说明存储卡IO操作异常
            }

            //指纹文件创建成功
            tmp3.add(mount);
        }
        //删除指纹文件
        for (StorageItem item : tmp3) {
            deleteFingerPrint(item, context, fingerName);
        }

        return new ArrayList<>(tmp3);
    }

    /**
     * 通过System.getenv()方法获取SDCard路径
     * @deprecated 该方法不靠谱, 很多机型上都获取不到
     */
    @Deprecated
    private static ArrayList<StorageItem> getStorageListBySystemEnv(Context context) {
        ArrayList<StorageItem> arrayList = new ArrayList<>();
        // 获取第一张sd卡
        String sdcardPath = System.getenv("EXTERNAL_STORAGE");
        if (checkPathValidate(sdcardPath)) {
            StorageItem info = new StorageItem(sdcardPath, context);
            if (info.canWrite(context) && Environment.MEDIA_MOUNTED.equals(info.mState)) {
                arrayList.add(info);
            }
        }

        //获取第二张sd卡
        String extSdcardPath = System.getenv("SECONDARY_STORAGE");
        if (!TextUtils.isEmpty(extSdcardPath)) {
            // There may be may devices split by :
            String[] split = extSdcardPath.split(":");
            for (String extPath : split) {
                if (!TextUtils.equals(extPath, sdcardPath) && checkPathValidate(extPath)) {
                    StorageItem info = new StorageItem(extPath, context);
                    //
                    if (info.canWrite(context) && Environment.MEDIA_MOUNTED.equals(info.mState)) {
                        arrayList.add(info);
                    }
                }
            }
        }

        return arrayList;
    }

    /**
     * 校验根路径的有效性
     */
    private static boolean checkPathValidate(String path) {
        File file = new File(path);

        return file.exists() && file.isDirectory();
    }

    /**
     * 是否有指纹文件
     */
    private static boolean hasFingerPrint(StorageItem item, Context context, String fingerName) {

        String innerPath = item.mPath + "/Android/data/" + context.getPackageName() + "/files";
        File fp = new File(innerPath, fingerName);

        return fp.exists();
    }

    /**
     * 删除指纹文件
     */
    private static boolean deleteFingerPrint(StorageItem item, Context context, String fingerName) {

        String innerPath = item.mPath + "/Android/data/" + context.getPackageName() + "/files";
        File fp = new File(innerPath, fingerName);

        return !fp.exists() || fp.delete();
    }

    /**
     * 创建指纹文件
     */
    private static boolean createFingerPrint(StorageItem item, Context context, String fingerName) {

        String innerPath = item.mPath + "/Android/data/" + context.getPackageName() + "/files";
        File fp = new File(innerPath, fingerName);

        boolean sucess = false;
        try {
            if (!fp.exists()) {
                sucess = fp.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(fp);
            fos.write("finger".getBytes());
            fos.flush();
            fos.close();

            sucess = true;
        } catch (IOException e) {
            sucess = false;
        }

        return sucess;
    }


    /**
     * 存储卡基本信息
     */
    public static class StorageItem {
        /**
         * 存储路径
         */
        public String mPath;
        /**
         * 描述信息
         */
        public String mDescription;
        /**
         * 存储卡类型，内置SD卡、外置SD卡、外置USB存储
         */
        public int mType = MEDIA_UNKNOWN;
        /**
         * UUID, 内置卡的uuid为null，外置卡才有该标识
         */
        public String mUuid;
        /**
         * 是否主卡
         */
        public boolean mPrimary = false;
        /**
         * 是否可移除的，区分内置卡和外置卡
         */
        public boolean mRemovable = true;
        /**
         * 是否模拟的分区
         */
        public boolean mEmulated = false;
        /**
         * 当前挂载状态
         */
        public String mState;


        StorageItem(String path) {

            mPath = path;
            mRemovable = false;
            mEmulated = false;
            mState = Environment.MEDIA_MOUNTED;
            mType = INTERNAL_STORAGE;
        }

        /**
         * 通过mount表实现的构造方法
         *
         * @param path 存储卡根路径
         */
        StorageItem(String path, Context context) {

            mPath = path;
            mRemovable = isRemovable();
            mEmulated = isEmulated();
            if (!mRemovable) {
                mPrimary = true;  // 通常不可移除的卡视为主卡
            }
            mState = getState(context);
            mType = getStorageType();
        }

        /**
         * Android 7.0使用{@link StorageVolume}访问
         *
         * @param storageVolume  存储卡StorageVolume实例
         * @param context 上下文
         */
        @TargetApi(Build.VERSION_CODES.N)
        StorageItem(StorageVolume storageVolume, Context context) {

            // getPath is hide
            try {
                Method m = storageVolume.getClass().getMethod("getPath");
                m.setAccessible(true);

                mPath = (String) m.invoke(storageVolume);
            } catch (Exception e) {
                /* ignore */
            }

            mDescription = storageVolume.getDescription(context);
            mUuid = storageVolume.getUuid();
            mState = storageVolume.getState();
            mRemovable = storageVolume.isRemovable();
            mPrimary = storageVolume.isPrimary();
            mEmulated = storageVolume.isEmulated();
            // 存储卡类型
            mType = getStorageType();
        }

        /**
         * 通过反射{@link android.os.storage.StorageVolume}的构造函数
         *
         * @param storageVolume StorageVolume类的实例,反射其方法初始化StorageInfo
         * @param context  上下文
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        StorageItem(Object storageVolume, Context context) throws IllegalAccessException, InvocationTargetException {
            // 遍历StorageVolume所有方法
            for (Method m : storageVolume.getClass().getDeclaredMethods()) {

                if (m.getName().equals("getPath") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class) {
                    mPath = (String) m.invoke(storageVolume); // above Android 4.0
                }

                if (m.getName().equals("getDescription") && m.getReturnType() == String.class) {
                    if (m.getParameterTypes().length == 0) {
                        mDescription = (String) m.invoke(storageVolume); // Android 4.0
                    } else if (m.getParameterTypes().length == 1) {
                        mDescription = (String) m.invoke(storageVolume, context); // above Android 4.1
                    }
                }

                if (m.getName().equals("getUuid") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class) {
                    mUuid = (String) m.invoke(storageVolume);  // above Android 4.4.1
                }

                if (m.getName().equals("getState") && m.getParameterTypes().length == 0 && m.getReturnType() == String.class) {
                    mState = (String) m.invoke(storageVolume); // above Android 4.4.1
                }

                if (m.getName().equals("isRemovable") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class) {
                    mRemovable = (Boolean) m.invoke(storageVolume); // above Android 4.0
                }

                if (m.getName().equals("isPrimary") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class) {
                    mPrimary = (Boolean) m.invoke(storageVolume); // above Android 4.2
                }

                if (m.getName().equals("isEmulated") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class) {
                    mEmulated = (Boolean) m.invoke(storageVolume); // above Android 4.0
                }

            }

            if (TextUtils.isEmpty(mState)) {
                // 4.0-4.4的机型无法通过反射StorageVolume获取状态信息
                mState = getState(context);
            }
            //存储卡类型
            mType = getStorageType();
        }


        /**
         * 检查给定SD卡应用特定目录/Android/data/{package_name}/files目录是否可读
         * 不能直接检测根目录，因为需要申请WRITE_EXTERNAL_STORAGE权限及Android 6.0的动态权限
         */
        protected boolean canRead(Context context) {

            String appFilesPath = mPath + "/Android/data/" + context.getPackageName() + "/files";
            File appFile = new File(appFilesPath);
            if (!appFile.exists()) {
                // This is important for Android 4.4+, secondary storage write permission is limited
                // The directory may be deleted by other process, so delegate the system to make dir
                context.getExternalFilesDir(null);
                // Then try to make dir ourself
                appFile.mkdirs();
            }

            return appFile.exists() && appFile.canRead();
        }

        /**
         * 检查给定SD卡根路径下的/Android/data/{package_name}/files目录是否可写
         * 不能直接检测根目录，因为需要申请WRITE_EXTERNAL_STORAGE权限及Android 6.0的动态权限
         */
        protected boolean canWrite(Context context) {

            String appFilesPath = mPath + "/Android/data/" + context.getPackageName() + "/files";
            File appFile = new File(appFilesPath);
            if (!appFile.exists()) {
                // This is important for Android 4.4+, secondary storage write permission is limited
                // The directory may be deleted by other process, so delegate the system to make dir
                context.getExternalFilesDir(null);
                // Then try to make dir ourself
                appFile.mkdirs();
            }

            return appFile.canWrite();
        }

        /**
         * 获取当前存储分区的类型
         *
         * @return 存储卡类型
         * @see StorageUtil#INTERNAL_STORAGE
         * @see StorageUtil#EXTERNAL_SDCARD
         * @see StorageUtil#EXTERNAL_USB
         * @see StorageUtil#MEDIA_UNKNOWN
         */
        private int getStorageType() {
            int type = MEDIA_UNKNOWN;

            String tmp = mPath.toLowerCase();
            if (!mRemovable) {
                // 不可移除, 内置卡
                type = INTERNAL_STORAGE;
            } else if (tmp.contains("usb") || tmp.contains("udisk") || tmp.contains("otg")) {
                // 可以移除，含有usb, udisk, otg字样, 是USB存储
                type = EXTERNAL_USB;
            } else {
                // 可以移除, 外置SD卡
                type = EXTERNAL_SDCARD;
            }

            return type;
        }


        /**
         * 获取当前存储分区的状态
         */
        private String getState(Context context) {

            String state = null;
            //反射StorageManager的getVolumeState方法, 4.0+
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Method m = sm.getClass().getMethod("getVolumeState", String.class);
                m.setAccessible(true);

                state = (String) m.invoke(sm, mPath);
            } catch (Exception e) {
                /* ignore */
            }

            if (state != null) {
                return state;
            }

            File file = new File(mPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0+
                state = Environment.getExternalStorageState(file);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Android 4.4+
                state = Environment.getStorageState(file);
            } else {
                if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    // external primary storage
                    state = Environment.getExternalStorageState();
                } else if (canRead(context) && getTotalSize() > 0) {
                    // readable and has volume, Android 2.3
                    state = Environment.MEDIA_MOUNTED;
                } else {
                    //
                    state = EnvironmentCompat.MEDIA_UNKNOWN;
                }
            }

            return state;
        }


        /**
         * 判断当前存储分区是否可以移除
         */
        private boolean isRemovable() {
            boolean removable = true;  // 默认是可移除的

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    removable = Environment.isExternalStorageRemovable(new File(mPath));
                } catch (IllegalArgumentException e) {
                    /* ignore */
                }
            } else {
                if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    // external primary storage
                    removable = Environment.isExternalStorageRemovable();
                }
            }

            return removable;
        }

        /**
         * 判断当前存储分区是否是模拟的
         */
        private boolean isEmulated() {
            boolean emulated = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    emulated = Environment.isExternalStorageEmulated(new File(mPath));
                } catch (IllegalArgumentException e) {
                    /* ignore */
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (mPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    // external primary storage
                    emulated = Environment.isExternalStorageEmulated();
                }
            }

            return emulated;
        }

        /**
         * 获取当前存储的总容量
         */
        public long getTotalSize() {

            long totalSize = 0L;

            File file = new File(mPath);
            totalSize = file.getTotalSpace();

//            StatFs statFs = new StatFs(mPath);
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
//                totalSize = statFs.getTotalBytes();
//            }else {
//                long blockSize = statFs.getBlockSize();
//                long blockCount = statFs.getBlockCount();
//                totalSize = blockSize * blockCount;
//            }

            return totalSize;
        }

        /**
         * 获取当前存储的可用容量
         */
        public long getAvailableSize() {

            long availSize = 0L;

            File file = new File(mPath);
            availSize = file.getFreeSpace();  // file.getUsableSpace() is really available for normal app

//            StatFs statFs = new StatFs(mPath);
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
//                availSize = statFs.getFreeBytes();  // statFs.getAvailableBytes() is really available for normal app
//            }else {
//                long blockSize = statFs.getBlockSize();
//                long blockCount = statFs.getFreeBlocks();  // statFs.getAvailableBlocks() is really available for normal app
//                availSize = blockSize * blockCount;
//            }

            return availSize;
        }


        /**
         * 获取当前存储已用的容量
         */
        public long getUsedSize() {

            long totalSize = getTotalSize();
            long availSize = getAvailableSize();

            return totalSize - availSize;
        }


        @Override
        public String toString() {

            long totalSize = getTotalSize();
            long availSize = getAvailableSize();
            long usedSize = totalSize - availSize;

            return "StorageItem{type=" + mType + ", path=" + mPath + ", description=" + mDescription
                    + ", uuid=" + mUuid + ", state=" + mState + ", primary=" + mPrimary
                    + ", removable=" + mRemovable + ", emulated=" + mEmulated + ", totalSize=" + totalSize
                    + ", usedSize=" + usedSize + ", availSize=" + availSize + "}";
        }
    }
}