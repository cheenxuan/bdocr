package me.xuan.bdocr.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.UUID;

/**
 * Author: xuan
 * Created on 2019/10/23 14:34.
 * <p>
 * Describe:
 */
public class DeviceUtil {
    public DeviceUtil() {
    }

    public static String getVersionName(Context context) {
        String versionName = "";

        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packInfo.versionName;
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return versionName;
    }

    public static String getDeviceId(Context context) {
        SharedPreferences sp = context.getSharedPreferences("ocr_sdk_uuid", 0);
        String uuid = sp.getString("uuid", "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("uuid", uuid);
            editor.commit();
        }

        return uuid == null ? "" : uuid;
    }

    public static String getBuildVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Android#");
        sb.append(getBuildVersion()).append("#");
        sb.append(Build.BRAND).append("|");
        sb.append(Build.MODEL).append("|");
        sb.append(Build.BOARD).append("#");
        sb.append(getDeviceId(context));
        return sb.toString();
    }
}
