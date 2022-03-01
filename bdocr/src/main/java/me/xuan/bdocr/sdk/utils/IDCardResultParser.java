package me.xuan.bdocr.sdk.utils;

import android.text.TextUtils;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.IDCardResult;
import me.xuan.bdocr.sdk.model.Word;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: xuan
 * Created on 2019/10/23 15:09.
 * <p>
 * Describe:
 */
public class IDCardResultParser implements Parser<IDCardResult> {
    private String idCardSide;

    public IDCardResultParser(String idCardSide) {
        this.idCardSide = idCardSide;
    }

    public IDCardResult parse(String json) throws OCRError {
        OCRError error;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code")) {
                error = new OCRError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                error.setLogId(jsonObject.optLong("log_id"));
                throw error;
            } else {
                IDCardResult result = new IDCardResult();
                result.setLogId(jsonObject.optLong("log_id"));
                result.setJsonRes(json);
                result.setDirection(jsonObject.optInt("direction", -1));
                result.setWordsResultNumber(jsonObject.optInt("words_result_num"));
                result.setRiskType(jsonObject.optString("risk_type"));
                result.setIdcardNumberType(jsonObject.optString("idcard_number_type"));
                result.setImageStatus(jsonObject.optString("image_status"));
                JSONObject cardQualityResult = jsonObject.optJSONObject("card_quality");
                if (cardQualityResult != null) {
                    try {
                        result.setIsClear(cardQualityResult.optInt("IsClear"));
                        result.setIsNoCover(cardQualityResult.optInt("IsNoCover"));
                        result.setIsComplete(cardQualityResult.optInt("IsComplete"));
                        result.setIsClearPropobility(cardQualityResult.optDouble("IsClear_propobility"));
                        result.setIsNoCoverPropobility(cardQualityResult.optDouble("IsNoCover_propobility"));
                        result.setIsCompletePropobility(cardQualityResult.optDouble("IsComplete_propobility"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                JSONObject wordResult = jsonObject.optJSONObject("words_result");
                if (TextUtils.isEmpty(this.idCardSide)) {
                    this.idCardSide = "front";
                }

                result.setIdCardSide(this.idCardSide);
                if (wordResult != null) {
                    if ("front".equals(this.idCardSide)) {
                        result.setAddress(this.map(wordResult.optJSONObject("住址")));
                        result.setIdNumber(this.map(wordResult.optJSONObject("公民身份号码")));
                        result.setBirthday(this.map(wordResult.optJSONObject("出生")));
                        result.setGender(this.map(wordResult.optJSONObject("性别")));
                        result.setName(this.map(wordResult.optJSONObject("姓名")));
                        result.setEthnic(this.map(wordResult.optJSONObject("民族")));
                    } else if ("back".equals(this.idCardSide)) {
                        result.setSignDate(this.map(wordResult.optJSONObject("签发日期")));
                        result.setExpiryDate(this.map(wordResult.optJSONObject("失效日期")));
                        result.setIssueAuthority(this.map(wordResult.optJSONObject("签发机关")));
                    }
                }

                return result;
            }
        } catch (JSONException var5) {
            error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "Server illegal response " + json, var5);
            throw error;
        }
    }

    private Word map(JSONObject jsonObject) {
        Word word = null;
        if (jsonObject != null) {
            word = new Word();
            JSONObject locationObject = jsonObject.optJSONObject("location");
            word.getLocation().setLeft(locationObject.optInt("left"));
            word.getLocation().setTop(locationObject.optInt("top"));
            word.getLocation().setWidth(locationObject.optInt("width"));
            word.getLocation().setHeight(locationObject.optInt("height"));
            word.setWords(jsonObject.optString("words"));
        }

        return word;
    }
}