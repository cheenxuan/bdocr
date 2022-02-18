package me.xuan.bdocr.sdk.utils;

import org.json.JSONException;
import org.json.JSONObject;

import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.AccessToken;

import static me.xuan.bdocr.sdk.exception.SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR;

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
            if (!jsonObject.isNull("status")) {
                int status = jsonObject.optInt("status");
                if (status == 0) {
                    JSONObject data = jsonObject.optJSONObject("data");
                    if (data != null) {
                        AccessToken accessToken = new AccessToken();
                        accessToken.setTokenJson(json);
                        accessToken.setAccessToken(data.getString("access_token"));
                        if (data.has("lic")) {
                            accessToken.setLic(data.getString("lic"));
                        }

                        accessToken.setExpiresIn(data.optInt("expires_in"));
                        try {
                            accessToken.setExpireTime(System.currentTimeMillis());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return accessToken;
                    } else {
                        return null;
                    }
                } else {
                    String message = jsonObject.optString("message");
                    Long logId = jsonObject.optLong("log_id");
                    throw new SDKError(status, message + " logId: " + logId);
                }
            } else {
                error = new SDKError(ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json);
                throw error;
            }
        } catch (JSONException e) {
            throw new SDKError(ACCESS_TOKEN_DATA_ERROR, "Server illegal response " + json, e);
        }
    }
}