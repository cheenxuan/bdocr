package me.xuan.bdocr.sdk.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import me.xuan.bdocr.sdk.OCR;
import me.xuan.bdocr.sdk.OnResultListener;
import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
import me.xuan.bdocr.sdk.model.AccessToken;
import me.xuan.bdocr.sdk.model.RequestParams;
import me.xuan.bdocr.sdk.utils.HttpsClient.Callback;
import me.xuan.bdocr.sdk.utils.HttpsClient.RequestBody;
import me.xuan.bdocr.sdk.utils.HttpsClient.RequestInfo;

/**
 * Author: xuan
 * Created on 2019/10/23 14:39.
 * <p>
 * Describe:
 */
public class HttpUtil {
    private Handler handler;
    private static volatile HttpUtil instance;
    private static HttpUtil.Options options = new HttpUtil.Options();

    private HttpUtil() {
    }

    public static void setOptions(HttpUtil.Options options) {
        HttpUtil.options = options;
    }

    public static HttpUtil.Options getOptions() {
        return options;
    }

    public static HttpUtil getInstance() {
        if (instance == null) {
            Class var0 = HttpUtil.class;
            synchronized(HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }

        return instance;
    }

    public void init() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    public <T> void post(String path, RequestParams params, final Parser<T> parser, final OnResultListener<T> listener) {
        HttpsClient cl = new HttpsClient();
        RequestBody body = new RequestBody();
        body.setStrParams(params.getStringParams());
        body.setFileParams(params.getFileParams());
        RequestInfo reqInfo = new RequestInfo(path, body);
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new Callback() {
            public void onFailure(final Throwable e) {
                HttpUtil.this.handler.post(new Runnable() {
                    public void run() {
                        HttpUtil.throwSDKError(listener, 283504, "Network error", e);
                    }
                });
            }

            public void onResponse(String resultStr) {
                String responseString = resultStr;

                try {
                    final T result = parser.parse(responseString);
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final OCRError var4) {
                    HttpUtil.this.handler.post(new Runnable() {
                        public void run() {
                            listener.onError(var4);
                        }
                    });
                }

            }
        });
    }

    public void getAccessToken(final OnResultListener<AccessToken> listener, String url, String param) {
        final Parser<AccessToken> accessTokenParser = new AccessTokenParser();
        HttpsClient cl = new HttpsClient();
        RequestBody body = new RequestBody();
        body.setBody(param);
        RequestInfo reqInfo = new RequestInfo(url, body);
        reqInfo.setHeader("Content-Type", "text/html");
        reqInfo.build();
        cl.newCall(reqInfo).enqueue(new Callback() {
            public void onFailure(Throwable e) {
                HttpUtil.throwSDKError(listener, 283504, "Network error", e);
            }

            public void onResponse(String resultStr) {
                if (resultStr != null && !TextUtils.isEmpty(resultStr)) {
                    try {
//                        System.out.println("HttpUtil -> getAccessToken -> " + resultStr);
                        AccessToken accessToken = (AccessToken)accessTokenParser.parse(resultStr);
                        if (accessToken != null) {
                            OCR.getInstance((Context)null).setAccessToken(accessToken);
                            OCR.getInstance((Context)null).setLicense(accessToken.getLic());
                            listener.onResult(accessToken);
                        } else {
                            HttpUtil.throwSDKError(listener, 283505, "Server illegal response " + resultStr);
                        }
                    } catch (SDKError var3) {
                        listener.onError(var3);
                    } catch (Exception var4) {
                        HttpUtil.throwSDKError(listener, 283505, "Server illegal response " + resultStr, var4);
                    }

                } else {
                    HttpUtil.throwSDKError(listener, 283505, "Server illegal response " + resultStr);
                }
            }
        });
    }

    private static void throwSDKError(OnResultListener listener, int errorCode, String msg) {
        SDKError error = new SDKError(errorCode, msg);
        listener.onError(error);
    }

    private static void throwSDKError(OnResultListener listener, int errorCode, String msg, Throwable cause) {
        SDKError error = new SDKError(errorCode, msg, cause);
        listener.onError(error);
    }

    public void release() {
        this.handler = null;
    }

    public static class Options {
        private int connectionTimeoutInMillis = 10000;
        private int socketTimeoutInMillis = 10000;

        public Options() {
        }

        public int getConnectionTimeoutInMillis() {
            return this.connectionTimeoutInMillis;
        }

        public void setConnectionTimeoutInMillis(int connectionTimeoutInMillis) {
            this.connectionTimeoutInMillis = connectionTimeoutInMillis;
        }

        public int getSocketTimeoutInMillis() {
            return this.socketTimeoutInMillis;
        }

        public void setSocketTimeoutInMillis(int socketTimeoutInMillis) {
            this.socketTimeoutInMillis = socketTimeoutInMillis;
        }
    }
}
