package me.xuan.bdocr.sdk.utils;


import org.json.JSONException;
import org.json.JSONObject;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.model.OcrResponseResult;

/**
 * Author: xuan
 * Created on 12/24/21 10:00 PM.
 * <p>
 * Describe:
 */
public class OcrResultParser implements Parser<OcrResponseResult> {
    public OcrResultParser() {
    }

    public OcrResponseResult parse(String json) throws OCRError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code")) {
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
            throw new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "Server illegal response " + json, var4);
        }
    }
}