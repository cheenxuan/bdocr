package me.xuan.bdocr.sdk.utils;

import org.json.JSONException;
import org.json.JSONObject;
import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.BankCardResult;

/**
 * Author: xuan
 * Created on 2019/10/23 15:16.
 * <p>
 * Describe:
 */
public class BankCardResultParser implements Parser<BankCardResult> {
    public BankCardResultParser() {
    }

    public BankCardResult parse(String json) throws OCRError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code")) {
                OCRError error = new OCRError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                error.setLogId(jsonObject.optLong("log_id"));
                throw error;
            } else {
                BankCardResult result = new BankCardResult();
                result.setLogId(jsonObject.optLong("log_id"));
                result.setDirection(jsonObject.optInt("direction", -1));
                result.setJsonRes(json);
                JSONObject resultObject = jsonObject.optJSONObject("result");
                if (resultObject != null) {
                    result.setBankCardNumber(resultObject.optString("bank_card_number"));
                    result.setBankCardType(resultObject.optInt("bank_card_type"));
                    result.setBankName(resultObject.optString("bank_name"));
                }

                return result;
            }
        } catch (JSONException var5) {
            throw new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "Server illegal response " + json, var5);
        }
    }
}