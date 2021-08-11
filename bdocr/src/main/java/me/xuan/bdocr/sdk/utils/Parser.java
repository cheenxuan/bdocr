package me.xuan.bdocr.sdk.utils;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;

/**
 * Author: xuan
 * Created on 2019/10/23 14:40.
 * <p>
 * Describe:
 */
public interface Parser<T> {
    T parse(String var1) throws OCRError, SDKError, OCRError;
}
