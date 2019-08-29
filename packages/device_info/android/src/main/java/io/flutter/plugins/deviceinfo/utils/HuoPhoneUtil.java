package io.flutter.plugins.deviceinfo.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by Amos Zhong on 2016/10/11.
 */

@SuppressLint("HardwareIds")
public class HuoPhoneUtil {
    public static String SP_KEY_DEVICE_ID = "sp_key_device_id";
    private static String deviceId;
    private static Integer openCnt;

    public static boolean isMainProcess(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
                if (processInfo.pid == pid) {
                    return TextUtils.equals(processInfo.processName, context.getPackageName());
                }
            }
        }

        return false;
    }

    /**
     * 获取ua信息
     *
     * @throws
     */
    public static String getUserUa() {
//        Caused by: java.lang.ClassNotFoundException: Didn't find class "android.webkit.TracingController" on path
        try {
            //api 19 之前
            if (Build.VERSION.SDK_INT < 19) {
                WebView webview = new WebView(ApplicationHolder.getApplication());
                WebSettings settings = webview.getSettings();
                return settings.getUserAgentString();
            } else {
                return WebSettings.getDefaultUserAgent(ApplicationHolder.getApplication());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 获取设备唯一号，依次获取IMEI, AndroidId，如果都没有就分配一个UUID，存储到prefs
     *
     * @return 设备唯一号 Device Id
     */
    public static String getDeviceId() {

        synchronized (HuoPhoneUtil.class) {
            Context context = ApplicationHolder.getApplication();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            if (TextUtils.isEmpty(deviceId)) {
                deviceId=sharedPreferences.getString(SP_KEY_DEVICE_ID,"");
//                deviceId = LocalApi.getInstance().getDeviceId();
            }

            // SharedPreferences没有存储的情况下，需要保存
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = PhoneUtils.getIMEI();
                if (TextUtils.isEmpty(deviceId)) {
                    deviceId = DeviceUtils.getAndroidId();
                }
                if (TextUtils.isEmpty(deviceId)) {
                    deviceId = PhoneUtils.getMEID();
                }
                if (TextUtils.isEmpty(deviceId)) {
                    deviceId = UUID.randomUUID().toString();
                }
                sharedPreferences.edit().putString(SP_KEY_DEVICE_ID,deviceId);
            }
        }
        return deviceId;
    }

    /**
     * 获取sdk 打开次数，从1开始
     *
     * @return 打开次数，从1开始
     */
    public static int getOpenCntAndAddCount() {
        if (openCnt == null) {
            Context context = ApplicationHolder.getApplication();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            openCnt = sharedPreferences.getInt(SP_KEY_DEVICE_ID, 0) + 1;
            if (openCnt == Integer.MAX_VALUE - 100) {//达到最大值
                openCnt = 1;
            }
            sharedPreferences.edit().putInt(SP_KEY_DEVICE_ID, openCnt).commit();
        }
        return openCnt;
    }

    /**
     * 获取操作系统
     */
    public static String getOS() {
        return "android";
    }

    /**
     * 获取操作系统版本号 Android 4.3
     */
    public static String getOSVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }

    /**
     * 获取设备类型
     */
    public static String getDeviceType() {
        return "Android";
    }

    /**
     * 获取手机厂商
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机型号
     *
     * @return 设备型号 HTC Hero
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * Return the width of screen, in pixel.
     *
     * @return the width of screen, in pixel
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) ApplicationHolder.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * Return the height of screen, in pixel.
     *
     * @return the height of screen, in pixel
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) ApplicationHolder.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return -1;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 获取手机分辨率
     *
     * @return 分辨率 如：2560*1600
     */
    public static String getResolution() {
        return getScreenWidth() + "*" + getScreenHeight();
    }

    /**
     * @return 手机服务商信息 ，如 中国移动 、中国电信
     */
    public static String getProvidersName() {
        String ProvidersName = "";
        try {
            String IMSI = PhoneUtils.getIMSI();
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProvidersName;
    }

    /**
     * 获取屏幕亮度
     *
     * @return 屏幕亮度 0-255
     */
    public static int getBrightness() {
        try {
            return Settings.System.getInt(
                    ApplicationHolder.getApplication().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 是否root
     *
     * @return
     */
    public static boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/",
                "/system/sbin/", "/usr/bin/", "/vendor/bin/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 总容量-已用
     */
    public static String getDiskState() {
        try {
            File root = Environment.getRootDirectory();
            StatFs sf = new StatFs(root.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getFreeBlocks();
            return (blockCount * blockSize) + "-" + (availCount * blockSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "none";
    }

    /**
     * 系统开机至今的时长，单位秒
     *
     * @return
     */
    public static long getOpenTime() {
        return SystemClock.elapsedRealtime() / 1000;
    }

}
