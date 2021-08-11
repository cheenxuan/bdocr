package me.xuan.bdocr.sdk.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: xuan
 * Created on 2019/10/23 14:50.
 * <p>
 * Describe:
 */
public class GeneralBasicParams implements RequestParams {
    public static final String CHINESE_ENGLISH = "CHN_ENG";
    public static final String ENGLISH = "ENG";
    public static final String PORTUGUESE = "POR";
    public static final String FRENCH = "FRE";
    public static final String GERMAN = "GER";
    public static final String ITALIAN = "ITA";
    public static final String SPANISH = "SPA";
    public static final String RUSSIAN = "RUS";
    public static final String JAPANESE = "JAP";
    private Map<String, String> params = new HashMap();
    private Map<String, File> fileMap = new HashMap();

    public GeneralBasicParams() {
    }

    public Map<String, File> getFileParams() {
        return this.fileMap;
    }

    public Map<String, String> getStringParams() {
        return this.params;
    }

    public void setLanguageType(String languageType) {
        this.putParam("language_type", languageType);
    }

    public void setDetectDirection(boolean detectDirection) {
        if (detectDirection) {
            this.putParam("detect_direction", "true");
        } else {
            this.putParam("detect_direction", "false");
        }

    }

    public void setDetectLanguage(boolean detectLanguage) {
        this.putParam("detect_language", detectLanguage);
    }

    public void setImageFile(File imageFile) {
        this.fileMap.put("image", imageFile);
    }

    public File getImageFile() {
        return (File)this.fileMap.get("image");
    }

    protected void putParam(String key, String value) {
        if (value != null) {
            this.params.put(key, value);
        } else {
            this.params.remove(key);
        }

    }

    protected void putParam(String key, boolean value) {
        if (value) {
            this.putParam(key, "true");
        } else {
            this.putParam(key, "false");
        }

    }
}
