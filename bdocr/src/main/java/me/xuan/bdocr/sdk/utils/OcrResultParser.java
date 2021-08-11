package me.xuan.bdocr.sdk.utils;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.OcrResponseResult;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: xuan
 * Created on 2019/10/23 15:17.
 * <p>
 * Describe:
 */
public class OcrResultParser implements Parser<OcrResponseResult> {
    public OcrResultParser() {
    }

    public OcrResponseResult parse(String json) throws OCRError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code") && jsonObject.getInt("error_code") != 0) {
                OCRError error = new OCRError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                error.setLogId(jsonObject.optLong("log_id"));
                throw error;
            } else {
                OcrResponseResult result = new OcrResponseResult();
                result.setLogId(jsonObject.optLong("log_id"));
                result.setJsonRes(json);
                return result;
            }
        } catch (JSONException var4) {
            throw new OCRError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, var4);
        }
    }
}