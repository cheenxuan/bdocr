package me.xuan.bdocr.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: xuan
 * Created on 2019/10/23 15:10.
 * <p>
 * Describe:
 */
public class BankCardParams extends BaseImageParams {

    private int imageQuality = 20;
    private boolean detectDirection;

    public BankCardParams() {
    }

    public int getImageQuality() {
        return this.imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public void setDetectDirection(boolean detectDirection) {
        this.detectDirection = detectDirection;
    }

    public Map<String, String> getStringParams() {
        Map<String, String> stringMap = new HashMap();
        if (this.detectDirection) {
            stringMap.put("detect_direction", "true");
        } else {
            stringMap.put("detect_direction", "false");
        }

        return stringMap;
    }

}