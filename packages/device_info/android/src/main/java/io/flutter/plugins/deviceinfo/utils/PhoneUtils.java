package io.flutter.plugins.deviceinfo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/08/02
 *     desc  : utils about phone
 * </pre>
 */
public final class PhoneUtils {

    private PhoneUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Return whether the device is phone.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isPhone() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * Return the unique device id.
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the unique device id
     */
    @SuppressLint("HardwareIds,MissingPermission")
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getDeviceId() {
        try {
            TelephonyManager tm = getTelephonyManager();
            String deviceId = tm.getDeviceId();
            if (!TextUtils.isEmpty(deviceId)) return deviceId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String imei = tm.getImei();
                if (!TextUtils.isEmpty(imei)) return imei;
                String meid = tm.getMeid();
                return TextUtils.isEmpty(meid) ? "" : meid;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Return the serial of device.
     *
     * @return the serial of device
     */
    @SuppressLint("HardwareIds,MissingPermission")
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getSerial() {
        try {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Build.getSerial() : Build.SERIAL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Return the IMEI.
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the IMEI
     */
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getIMEI() {
        return getImeiOrMeid(true);
    }

    /**
     * Return the MEID.
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the MEID
     */
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getMEID() {
        return getImeiOrMeid(false);
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getImeiOrMeid(boolean isImei) {
        try {
            TelephonyManager tm = getTelephonyManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isImei) {
                    return getMinOne(tm.getImei(0), tm.getImei(1));
                } else {
                    return getMinOne(tm.getMeid(0), tm.getMeid(1));
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String ids = getSystemPropertyByReflect(isImei ? "ril.gsm.imei" : "ril.cdma.meid");
                if (!TextUtils.isEmpty(ids)) {
                    String[] idArr = ids.split(",");
                    if (idArr.length == 2) {
                        return getMinOne(idArr[0], idArr[1]);
                    } else {
                        return idArr[0];
                    }
                }

                String id0 = tm.getDeviceId();
                String id1 = "";
                try {
                    Method method = tm.getClass().getMethod("getDeviceId", int.class);
                    id1 = (String) method.invoke(tm,
                            isImei ? TelephonyManager.PHONE_TYPE_GSM
                                    : TelephonyManager.PHONE_TYPE_CDMA);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (isImei) {
                    if (id0 != null && id0.length() < 15) {
                        id0 = "";
                    }
                    if (id1 != null && id1.length() < 15) {
                        id1 = "";
                    }
                } else {
                    if (id0 != null && id0.length() == 14) {
                        id0 = "";
                    }
                    if (id1 != null && id1.length() == 14) {
                        id1 = "";
                    }
                }
                return getMinOne(id0, id1);
            } else {
                String deviceId = tm.getDeviceId();
                if (isImei) {
                    if (deviceId != null && deviceId.length() >= 15) {
                        return deviceId;
                    }
                } else {
                    if (deviceId != null && deviceId.length() == 14) {
                        return deviceId;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getMinOne(String s0, String s1) {
        boolean empty0 = TextUtils.isEmpty(s0);
        boolean empty1 = TextUtils.isEmpty(s1);
        if (empty0 && empty1) return "";
        if (!empty0 && !empty1) {
            if (s0.compareTo(s1) <= 0) {
                return s0;
            } else {
                return s1;
            }
        }
        if (!empty0) return s0;
        return s1;
    }

    private static String getSystemPropertyByReflect(String key) {
        try {
            @SuppressLint("PrivateApi")
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method getMethod = clz.getMethod("get", String.class, String.class);
            return (String) getMethod.invoke(clz, key, "");
        } catch (Exception e) {/**/}
        return "";
    }

    /**
     * Return the IMSI.
     * <p>Must hold {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return the IMSI
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
//    @RequiresPermission(READ_PHONE_STATE)
    public static String getIMSI() {
        try {
            return getTelephonyManager().getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns the current phone type.
     *
     * @return the current phone type
     * <ul>
     * <li>{@link TelephonyManager#PHONE_TYPE_NONE}</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_GSM }</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_CDMA}</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_SIP }</li>
     * </ul>
     */
    public static int getPhoneType() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getPhoneType();
    }

    /**
     * Return whether sim card state is ready.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isSimCardReady() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * Return the sim operator name.
     *
     * @return the sim operator name
     */
    public static String getSimOperatorName() {
        TelephonyManager tm = getTelephonyManager();
        return tm.getSimOperatorName();
    }

    /**
     * Return the sim operator using mnc.
     *
     * @return the sim operator
     */
    public static String getSimOperatorByMnc() {
        TelephonyManager tm = getTelephonyManager();
        String operator = tm.getSimOperator();
        if (operator == null) return "";
        switch (operator) {
            case "46000":
            case "46002":
            case "46007":
            case "46020":
                return "中国移动";
            case "46001":
            case "46006":
            case "46009":
                return "中国联通";
            case "46003":
            case "46005":
            case "46011":
                return "中国电信";
            default:
                return operator;
        }
    }

    private static TelephonyManager getTelephonyManager() {
        return (TelephonyManager) ApplicationHolder.getApplication().getSystemService(Context.TELEPHONY_SERVICE);
    }
}
