package me.xuan.bdocr.sdk.model;

/**
 * Author: xuan
 * Created on 2019/10/23 14:51.
 * <p>
 * Describe:
 */
public class ResponseResult {
    public static final int DIRECTION_UNSPECIFIED = -1;
    private long logId;
    private String jsonRes;

    public ResponseResult() {
    }

    public long getLogId() {
        return this.logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getJsonRes() {
        return this.jsonRes;
    }

    public void setJsonRes(String jsonRes) {
        this.jsonRes = jsonRes;
    }
}
