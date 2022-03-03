package me.xuan.baiduocr;


import android.util.Log;

/**
 * Author: xuan
 * Created on 2019/4/8 10:26.
 * <p>
 * Describe:
 */
public class LogUtil {

    private static String TAG = "BDOCR";
    /**
     * set IS_DEBUG false when release to close log.
     */
//    private static boolean IS_DEBUG = true;
    private static boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void i(String message) {
        if(IS_DEBUG) {
            Log.i(TAG ,"-->:"+message);
        }
    }

    public static void e(String message) {
        if(IS_DEBUG) {
            Log.e(TAG,"-->:"+message);
        }
    }

    public static void e(String message,Throwable e) {
        if(IS_DEBUG) {
            Log.e(TAG,"-->:"+ message,e);
        }
    }

    public static void w(String message) {
        if(IS_DEBUG) {
            Log.w(TAG,"-->:"+ message);
        }
    }

    public static void v(String message) {
        if(IS_DEBUG) {
            Log.v(TAG,"-->:"+ message);
        }
    }

    public static void d(String message) {
        if(IS_DEBUG) {
            Log.d(TAG ,"-->:"+ message);
        }
    }

    public static void d(String message,String msgObj) {
        if(IS_DEBUG) {
            Log.d(TAG , "-->:"+String.format(message,msgObj));
        }
    }
}
