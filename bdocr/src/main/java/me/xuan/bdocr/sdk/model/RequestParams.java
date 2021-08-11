package me.xuan.bdocr.sdk.model;

import java.io.File;
import java.util.Map;

/**
 * Author: xuan
 * Created on 2019/10/23 14:27.
 * <p>
 * Describe:
 */
public interface RequestParams {
    Map<String, File> getFileParams();

    Map<String, String> getStringParams();
}
