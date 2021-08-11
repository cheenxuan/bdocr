package me.xuan.bdocr.sdk;


import me.xuan.bdocr.sdk.exception.OCRError;

/**
 * Author: xuan
 * Created on 2019/10/23 14:41.
 * <p>
 * Describe:
 */
public interface OnResultListener<T> {
    void onResult(T var1);

    void onError(OCRError var1);
}
