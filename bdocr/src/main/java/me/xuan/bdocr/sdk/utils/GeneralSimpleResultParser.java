package me.xuan.bdocr.sdk.utils;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.GeneralResult;
import me.xuan.bdocr.sdk.model.WordSimple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Author: xuan
 * Created on 2019/10/23 15:07.
 * <p>
 * Describe:
 */
public class GeneralSimpleResultParser implements Parser<GeneralResult> {
    public GeneralSimpleResultParser() {
    }

    public GeneralResult parse(String json) throws OCRError {
        OCRError error;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code")) {
                error = new OCRError(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                error.setLogId(jsonObject.optLong("log_id"));
                throw error;
            } else {
                GeneralResult result = new GeneralResult();
                result.setLogId(jsonObject.optLong("log_id"));
                result.setJsonRes(json);
                result.setDirection(jsonObject.optInt("direction", -1));
                result.setWordsResultNumber(jsonObject.optInt("words_result_num"));
                JSONArray wordsArray = jsonObject.optJSONArray("words_result");
                int wordsArrayCount = wordsArray == null ? 0 : wordsArray.length();
                ArrayList<WordSimple> wordList = new ArrayList();

                for(int i = 0; i < wordsArrayCount; ++i) {
                    JSONObject wordObject = wordsArray.optJSONObject(i);
                    WordSimple word = new WordSimple();
                    word.setWords(wordObject.optString("words"));
                    wordList.add(word);
                }

                result.setWordList(wordList);
                return result;
            }
        } catch (JSONException var10) {
            error = new OCRError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, var10);
            throw error;
        }
    }
}