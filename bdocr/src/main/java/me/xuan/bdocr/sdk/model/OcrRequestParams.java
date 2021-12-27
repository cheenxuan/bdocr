package me.xuan.bdocr.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xuan
 * Created on 12/24/21 9:55 PM.
 * <p>
 * Describe:
 */
public class OcrRequestParams extends BaseImageParams implements RequestParams {
    private Map<String, String> params = new HashMap();

    public OcrRequestParams() {
    }

    public Map<String, String> getStringParams() {
        return this.params;
    }

    public void putParam(String key, String value) {
        if (value != null) {
            this.params.put(key, value);
        } else {
            this.params.remove(key);
        }

    }

    public void putParam(String key, boolean value) {
        if (value) {
            this.putParam(key, "true");
        } else {
            this.putParam(key, "false");
        }

    }

    public void putParam(String key, long value) {
        this.putParam(key, String.valueOf(value));
    }
}