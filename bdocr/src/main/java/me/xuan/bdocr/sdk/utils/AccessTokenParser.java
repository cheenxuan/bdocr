package me.xuan.bdocr.sdk.utils;

import org.json.JSONException;
import org.json.JSONObject;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.AccessToken;

/**
 * Author: xuan
 * Created on 2019/10/23 14:47.
 * <p>
 * Describe:
 */
public class AccessTokenParser implements Parser<AccessToken> {
    public AccessTokenParser() {
    }

    public AccessToken parse(String json) throws SDKError {
        SDKError error;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if(!jsonObject.isNull("access_token")) {
                AccessToken accessToken = new AccessToken();
                accessToken.setTokenJson(json);
                accessToken.setAccessToken(jsonObject.getString("access_token"));
                if(jsonObject.has("expires_in")) {
                    accessToken.setExpiresIn(jsonObject.getInt("expires_in"));
                }
                return accessToken;
            }else{
                if(!jsonObject.isNull("error")){
                    error = new SDKError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + jsonObject.getString("error_description"));
                    throw error;
                }else{
                    error = new SDKError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json);
                    throw error;
                }
            }

            //            if(!jsonObject.isNull("status")) {
            //                int status = jsonObject.optInt("status");
            //                if(status == 0) {
            //
            //                    if(data != null) {
            //                        AccessToken accessToken = new AccessToken();
            //                        accessToken.setTokenJson(json);
            //                        accessToken.setAccessToken(data.getString("access_token"));
            //                        if(data.has("lic")) {
            //                            accessToken.setLic(data.getString("lic"));
            //                        }
            //
            //                        accessToken.setExpiresIn(data.optInt("expires_in"));
            //                        return accessToken;
            //                    } else {
            //                        return null;
            //                    }
            //                } else {
            //                    String message = jsonObject.optString("message");
            //                    Long logId = jsonObject.optLong("log_id");
            //                    error = new SDKError(status, message + " logId: " + logId);
            //                    throw error;
            //                }
            //            } else {
            //                error = new SDKError(283505, "Server illegal response " + json);
            //                throw error;
            //            }
        } catch (JSONException var7) {
            error = new SDKError(SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, var7);
            throw error;
        }
    }
}