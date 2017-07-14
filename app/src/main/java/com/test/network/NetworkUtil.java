package com.test.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by softwise on 2017/7/11.
 */

public class NetworkUtil{

    /*
     * 获取手机网络类型（2G/3G/4G）：
     * 4G为LTE，
     * 联通的3G为UMTS或HSDPA，
     * 电信的3G为EVDO，
     * 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA。
     */
    public static int getNetWorkClass(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NetworkConstants.NETWORK_CLASS_2_G;

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NetworkConstants.NETWORK_CLASS_3_G;

            case TelephonyManager.NETWORK_TYPE_LTE:
                return NetworkConstants.NETWORK_CLASS_4_G;

            default:
                return NetworkConstants.NETWORK_CLASS_UNKNOWN;
        }
    }

    /*
     * 获取手机连接的网络类型（是WIFI还是手机网络[2G/3G/4G]）
     */
    public static int getNetWorkStatus(Context context) {
        int netWorkType = NetworkConstants.NETWORK_CLASS_UNKNOWN;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();

            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = NetworkConstants.NETWORK_WIFI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                netWorkType = getNetWorkClass(context);
            }
        }
        return netWorkType;
    }

    //设置网络优先级
    public static void setPreferredNetwork(Context context, int networkType) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //设为网络优先
        connectivityManager.setNetworkPreference(networkType);

    }

    /**
     * 返回手机移动数据的状态
     *
     * @param pContext
     * @param arg
     *            默认填null
     * @return true 连接 false 未连接
     */
    public static boolean getMobileDataState(Context pContext, Object[] arg) {
        Boolean isOpen = false;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //andriod 5.0 （API 21）以下  通过此反射方法获取移动网络状态
                ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                Class ownerClass = mConnectivityManager.getClass();
                Class[] argsClass = null;
                if (arg != null) {
                    argsClass = new Class[1];
                    argsClass[0] = arg.getClass();
                }
                Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);
                isOpen = (Boolean) method.invoke(mConnectivityManager, arg);
                return isOpen;
            } else {
                //andriod 5.0 （API 21）以上  通过此反射方法获取移动网络状态
                TelephonyManager telephonyService = (TelephonyManager) pContext.getSystemService(Context.TELEPHONY_SERVICE);
                Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
                if (null != getMobileDataEnabledMethod){
                    isOpen = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                }
                return isOpen;
            }
        } catch (Exception e) {
            System.out.println("得到移动数据状态出错");
            return false;
        }
    }

    /**
     * 打开或关闭手机的移动数据
     */
    public static void setMobileData(Context pContext, boolean pBoolean) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //andriod 5.0 （API 21）以下  通过此方法打开或关闭移动网络
                ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                Class ownerClass = mConnectivityManager.getClass();
                Class[] argsClass = new Class[1];
                argsClass[0] = boolean.class;
                Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);
                method.invoke(mConnectivityManager, pBoolean);
            }else{
                //andriod 5.0 （API 21）以上  通过此方法打开或关闭移动网络
                TelephonyManager telephonyService = (TelephonyManager) pContext.getSystemService(Context.TELEPHONY_SERVICE);
                Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                if (null != setMobileDataEnabledMethod)                {
                    setMobileDataEnabledMethod.invoke(telephonyService, pBoolean);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("移动数据设置错误: " + e.toString());
        }
    }

    //判断wifi是否连通（不是是否开启）
    public static boolean isWiFiActive(Context inContext) {
        Context context = inContext.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //通过反射的方式去判断wifi是否已经连接上，并且可以开始传输数据
    private boolean checkWiFiConnectSuccess() {
        Class classType = WifiInfo.class;
        try {
            Object invo = classType.newInstance();
            Object result = invo.getClass().getMethod("getMeteredHint").invoke(invo);
            return (boolean) result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
