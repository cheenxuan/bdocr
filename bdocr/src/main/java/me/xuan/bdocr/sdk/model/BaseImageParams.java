package me.xuan.bdocr.sdk.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: xuan
 * Created on 2019/10/23 15:10.
 * <p>
 * Describe:
 */
public class BaseImageParams implements RequestParams {
    protected Map<String, File> fileMap = new HashMap();

    public BaseImageParams() {
    }

    public Map<String, File> getFileParams() {
        return this.fileMap;
    }

    public Map<String, String> getStringParams() {
        return null;
    }

    public void setImageFile(File imageFile) {
        this.fileMap.put("image", imageFile);
    }

    public File getImageFile() {
        return (File) this.fileMap.get("image");
    }
}