package com.pyy.findkids;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SNAS on 2016/10/21 0021.
 */

public class Receiver extends BroadcastReceiver {
    static final String logTag = "[Findkids]";
    static String filePath = "/sdcard/Findkids/";
    static String fileName = "info.txt";
    // For FTP
    static String ftpFile = filePath + fileName;
    // For SMS
    static int subId = -1;
    private static String lastAddr = null;

    // For write 2 file
    public Write2File wf = new Write2File();
    // For SMS
    public SubscriptionManager sManager = null;
    public TelephonyManager tm = null;

    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    public AMapLocationListener mapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            // For FTP
            String result = LocationUtils.getLocationStr(aMapLocation);
            // For SMS
            String msg = LocationUtils.getLocationShortStr(aMapLocation);

            // For write 2 file
            wf.writeTxtToFile(result, filePath, fileName);
            if (aMapLocation.getProvider().equals("lbs") || aMapLocation.getProvider().equals("gps")) {
                if (!aMapLocation.getAddress().equals(lastAddr)) {
                    // For SMS
                    if (msg != null) {
                        //获取卡1号码
                        String number = tm.getLine1Number();
                        //获取双卡信息， 通过sInfo来决定用哪个卡槽发消息
                        SubscriptionInfo sInfo = null;
                        List<SubscriptionInfo> list = sManager.getActiveSubscriptionInfoList();
                        if (list.size() == 2) {// double card
                            Log.d(logTag, "Double card");
                            for (int i = 0; i < 2; i++) {
                                sInfo = list.get(i);
                                if (sInfo != null) {
                                    // 通过选定的卡来确定移动联通电信：移动2，联通1，电信应该是0
                                    subId = sInfo.getSubscriptionId();
                                    //String phoneNumber = sInfo.getNumber();
                                }
                            }
                        } else {//single card
                            Log.d(logTag, "Single card");
                            sInfo = (list.get(0) == null) ? list.get(1) : list.get(0);
                            subId = sInfo.getSubscriptionId();
                        }
//Log.e(logTag, "Number " + number);  // 移动卡无法识别号码
                        if (subId == 2) {
                            SmsManager sms = SmsManager.getSmsManagerForSubscriptionId(subId); // subId决定卡槽
                            if (msg.length() > 70) {
                                ArrayList<String> msgs = sms.divideMessage(msg);
                                Log.d(logTag, "send Multi Text SMS");
                                sms.sendMultipartTextMessage("PHONE_NUMBER", null, msgs, null, null); // Phone number should add the country code. like +86
                            } else {
                                Log.d(logTag, "send Text SMS");
                                sms.sendTextMessage("PHONE_NUMBER", null, msg, null, null);
                            }
                        }
                        lastAddr = aMapLocation.getAddress();

                        // 以下是反射的实例
                /*
                SmsManager sms = SmsManager.getDefault();
                Class SMClass = SmsManager.class; //通过反射查到了SmsManager有个叫做mSubId的属性
                try {
                    Field field = SMClass.getDeclaredField("mSubId");
                    field.setAccessible(true);
                    field.set(sms,1); // 联通？
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
               } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                */
                    }
                }

            }

            // For FTP
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            FTP ftpclient = new FTP();
            File file = new File(ftpFile);
            try {
                ftpclient.uploadSingleFile(file, "Findkids");
            } catch (IOException e) {
                Log.e(logTag, "Exceptoion: " + e);
            }
        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {

        AMapLocationClient mlocationClient = new AMapLocationClient(context);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(mapLocationListener);
        //设置定位间隔,单位毫秒,默认为1800000ms, 即半个小时
        //mLocationOption.setInterval(30000);
        //设置为单次定位
        mLocationOption.setOnceLocation(true);
        // For SMS
        // getSystemService这个方法基于context
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        sManager = (SubscriptionManager)context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        // GPS on or off
        LocationManager locationManager = ((LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE));
        boolean isGpsAvalible = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Network active or inactive
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to wifi or connected to the mobile provider's data plan
                if (isGpsAvalible) {
                    Log.i(logTag, "Wifi/Mobile is On and GPS also on");
                    //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                } else {
                    Log.i(logTag, "Wifi/Mobile is On but GPS is off");
                    //设置定位模式为低功耗模式
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                }
            }
        } else {
            if (isGpsAvalible) {
                Log.i(logTag, "Wifi/Mobile is inactive but GPS is on");
                //设置定位模式为仅设备模式
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            } else {
                // not connected to the internet neither GPS
                Log.i(logTag, "Network is inactive and GPS also off");
                return;
            }
        }

        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
    }
}
