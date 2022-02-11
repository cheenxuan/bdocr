package me.xuan.bdocr.sdk.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: xuan
 * Created on 2019/10/23 14:28.
 * <p>
 * Describe:
 */
public class IDCardParams implements RequestParams {
    public static final String ID_CARD_SIDE_FRONT = "front";
    public static final String ID_CARD_SIDE_BACK = "back";
    private boolean detectDirection;
    private boolean detectRisk;
    private String idCardSide;
    private File imageFile;
    private int imageQuality = 20;

    public IDCardParams() {
    }

    public boolean isDetectDirection() {
        return this.detectDirection;
    }

    public void setDetectDirection(boolean detectDirection) {
        this.detectDirection = detectDirection;
    }

    public String getIdCardSide() {
        return this.idCardSide;
    }

    public void setIdCardSide(String idCardSide) {
        this.idCardSide = idCardSide;
    }

    public File getImageFile() {
        return this.imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    public void setDetectRisk(boolean detectRisk) {
        this.detectRisk = detectRisk;
    }

    public boolean getDetectRisk() {
        return this.detectRisk;
    }

    public int getImageQuality() {
        return this.imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public Map<String, File> getFileParams() {
        Map<String, File> fileMap = new HashMap();
        fileMap.put("image", this.imageFile);
        return fileMap;
    }

    public Map<String, String> getStringParams() {
        Map<String, String> stringMap = new HashMap();
        stringMap.put("id_card_side", this.idCardSide);
        if (this.detectDirection) {
            stringMap.put("detect_direction", "true");
        } else {
            stringMap.put("detect_direction", "false");
        }

        if (this.detectRisk) {
            stringMap.put("detect_risk", "true");
        } else {
            stringMap.put("detect_risk", "false");
        }

        return stringMap;
    }
}
