package me.xuan.bdocr.sdk.utils;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.GeneralResult;
import me.xuan.bdocr.sdk.model.VertexesLocation;
import me.xuan.bdocr.sdk.model.Word;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * Author: xuan
 * Created on 2019/10/23 15:06.
 * <p>
 * Describe:
 */
public class GeneralResultParser implements Parser<GeneralResult> {
    public GeneralResultParser() {
    }

    public GeneralResult parse(String json) throws OCRError {
        OCRError error;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("error_code") && jsonObject.getInt("error_code") != 0) {
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
                ArrayList<Word> wordList = new ArrayList();

                for(int i = 0; i < wordsArrayCount; ++i) {
                    JSONObject wordObject = wordsArray.optJSONObject(i);
                    JSONObject locationObject = wordObject.optJSONObject("location");
                    Word word = new Word();
                    word.getLocation().setLeft(locationObject.optInt("left"));
                    word.getLocation().setTop(locationObject.optInt("top"));
                    word.getLocation().setWidth(locationObject.optInt("width"));
                    word.getLocation().setHeight(locationObject.optInt("height"));
                    word.setWords(wordObject.optString("words"));
                    wordList.add(word);
                    JSONArray vertexesLocationArray = wordObject.optJSONArray("vertexes_location");
                    if (vertexesLocationArray != null) {
                        ArrayList<VertexesLocation> vertexesLocations = new ArrayList();

                        for(int j = 0; j < vertexesLocationArray.length(); ++j) {
                            JSONObject vertexesLocationObject = vertexesLocationArray.optJSONObject(j);
                            VertexesLocation vertexesLocation = new VertexesLocation();
                            vertexesLocation.setX(vertexesLocationObject.optInt("x"));
                            vertexesLocation.setY(vertexesLocationObject.optInt("y"));
                            vertexesLocations.add(vertexesLocation);
                        }

                        word.setVertexesLocations(vertexesLocations);
                    }

                    JSONArray charArray = wordObject.optJSONArray("chars");
                    if (charArray != null) {
                        ArrayList<Word.Char> charList = new ArrayList();

                        for(int j = 0; j < charArray.length(); ++j) {
                            JSONObject charObject = charArray.optJSONObject(j);
                            JSONObject location = charObject.optJSONObject("location");
                            Word.Char characterResult = new Word.Char();
                            characterResult.getLocation().setLeft(location.optInt("left"));
                            characterResult.getLocation().setTop(location.optInt("top"));
                            characterResult.getLocation().setWidth(location.optInt("width"));
                            characterResult.getLocation().setHeight(location.optInt("height"));
                            characterResult.setCharacter(charObject.optString("char"));
                            charList.add(characterResult);
                        }

                        word.setCharacterResults(charList);
                    }
                }

                result.setWordList(wordList);
                return result;
            }
        } catch (JSONException var18) {
            error = new OCRError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, var18);
            throw error;
        }
    }
}