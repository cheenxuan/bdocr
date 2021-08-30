package com.baidu.ocr.sdk.jni;

import android.content.Context;

import me.xuan.bdocr.sdk.exception.SDKError;


/**
 * Author: xuan
 * Created on 2019/10/23 15:20.
 * <p>
 * Describe:
 */
public class JniInterface {
    private static Throwable loadLibraryError;

    public JniInterface() {
    }

    public static Throwable getLoadLibraryError() {
        return loadLibraryError;
    }

    public native byte[] initWithBin(Context var1, String var2) throws SDKError;

    public native byte[] initWithBinLic(Context var1, String var2, String var3) throws SDKError;

    public native byte[] init(Context var1, String var2);

    public native String getToken(Context var1);

    public native String getTokenFromLicense(Context var1, byte[] var2, int var3);

    static {
        try {
            System.loadLibrary("ocr-sdk");
            loadLibraryError = null;
        } catch (Throwable var1) {
            loadLibraryError = var1;
        }

    }
}
