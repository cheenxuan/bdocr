package me.xuan.bdocr.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.File;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;

import com.baidu.ocr.sdk.jni.JniInterface;

import me.xuan.bdocr.sdk.model.AccessToken;
import me.xuan.bdocr.sdk.model.BankCardParams;
import me.xuan.bdocr.sdk.model.BankCardResult;
import me.xuan.bdocr.sdk.model.IDCardParams;
import me.xuan.bdocr.sdk.model.IDCardResult;
import me.xuan.bdocr.sdk.utils.AccessTokenParser;
import me.xuan.bdocr.sdk.utils.BankCardResultParser;
import me.xuan.bdocr.sdk.utils.CrashReporterHandler;
import me.xuan.bdocr.sdk.utils.DeviceUtil;
import me.xuan.bdocr.sdk.utils.HttpUtil;
import me.xuan.bdocr.sdk.utils.IDCardResultParser;
import me.xuan.bdocr.sdk.utils.ImageUtil;
import me.xuan.bdocr.sdk.utils.Parser;
import me.xuan.bdocr.sdk.utils.Util;

/**
 * Author: xuan
 * Created on 2019/10/23 14:29.
 * <p>
 * Describe:
 */
public class OCR {
    public static final String OCR_SDK_VERSION = "1_4_4";
    private static final String ID_CARD_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard?";
    private static final String BANK_CARD_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/bankcard?";
    private static final String QUERY_TOKEN = "https://verify.baidubce.com/verify/1.0/token/sk?sdkVersion=1_4_4";
    private static final String QUERY_TOKEN_BIN = "https://verify.baidubce.com/verify/1.0/token/bin?sdkVersion=1_4_4";
    private static final String PREFRENCE_FILE_KEY = "com.umpay.ocr.sdk";
    private static final String PREFRENCE_TOKENJSON_KEY = "token_json";
    private static final String PREFRENCE_EXPIRETIME_KEY = "token_expire_time";
    private static final String PREFRENCE_AUTH_TYPE = "token_auth_type";
    private static final int IMAGE_MAX_WIDTH = 1280;
    private static final int IMAGE_MAX_HEIGHT = 1280;
    private AccessToken accessToken = null;
    private static final int AUTHWITH_NOTYET = 0;
    private static final int AUTHWITH_LICENSE = 1;
    private static final int AUTHWITH_AKSK = 2;
    private static final int AUTHWITH_TOKEN = 3;
    private int authStatus = AUTHWITH_NOTYET;
    private String ak = null;
    private String sk = null;
    private boolean isAutoCacheToken = false;
    private String license = null;
    @SuppressLint({"StaticFieldLeak"})
    private Context context;
    //    private CrashReporterHandler crInst;
    private static volatile OCR instance;
    private CrashReporterHandler crInst;

    public boolean isAutoCacheToken() {
        return this.isAutoCacheToken;
    }

    public void setAutoCacheToken(boolean autoCacheToken) {
        this.isAutoCacheToken = autoCacheToken;
    }

    private OCR(Context ctx) {
        if (ctx != null) {
            this.context = ctx;
        }
    }

    public static OCR getInstance(Context ctx) {
        if (instance == null) {
            Class var1 = OCR.class;
            synchronized (OCR.class) {
                if (instance == null) {
                    instance = new OCR(ctx);
                }
            }
        }

        return instance;
    }

    public void init(Context context) {
        this.context = context;
        this.crInst = CrashReporterHandler.init(context).addSourceClass(OCR.class);

        try {
            Class uiClass = Class.forName("me.xuan.bdocr.ui.camera.CameraActivity");
            this.crInst.addSourceClass(uiClass);
        } catch (Throwable var3) {

        }

        HttpUtil.getInstance().init();
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public synchronized void setAccessToken(AccessToken accessToken) {
        if (accessToken.getTokenJson() != null) {
            SharedPreferences mSharedPreferences = this.context.getSharedPreferences(PREFRENCE_FILE_KEY, 0);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(PREFRENCE_TOKENJSON_KEY, accessToken.getTokenJson());
            editor.putLong(PREFRENCE_EXPIRETIME_KEY, accessToken.getExpiresTime());
            editor.putInt(PREFRENCE_AUTH_TYPE, this.authStatus);
            editor.apply();
        }

        this.accessToken = accessToken;
    }

    public synchronized AccessToken getAccessToken() {
        return this.accessToken;
    }


    public void recognizeIDCard(final IDCardParams param, final OnResultListener<IDCardResult> listener) {
        File imageFile = param.getImageFile();
        final File tempImage = new File(this.context.getCacheDir(), String.valueOf(System.currentTimeMillis()));
        ImageUtil.resize(imageFile.getAbsolutePath(), tempImage.getAbsolutePath(), IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, param.getImageQuality());
        param.setImageFile(tempImage);
        final Parser<IDCardResult> idCardResultParser = new IDCardResultParser(param.getIdCardSide());
        this.getToken(new OnResultListener() {
            public void onResult(Object result) {
                HttpUtil.getInstance().post(OCR.this.urlAppendCommonParams(ID_CARD_URL), param, idCardResultParser, new OnResultListener<IDCardResult>() {
                    public void onResult(IDCardResult result) {
                        tempImage.delete();
                        if (listener != null) {
                            if (result.isRecCorrect()) {
                                listener.onResult(result);
                            } else {
                                OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "recg id card error " + result.getImageStatus());
                                listener.onError(error);
                            }
                        }
                    }

                    public void onError(OCRError error) {
                        tempImage.delete();
                        if (listener != null) {
                            listener.onError(error);
                        }

                    }
                });
            }

            public void onError(OCRError error) {
                listener.onError(error);
            }
        });
    }

    public void recognizeBankCard(final BankCardParams params, final OnResultListener<BankCardResult> listener) {
        File imageFile = params.getImageFile();
        final File tempImage = new File(this.context.getCacheDir(), String.valueOf(System.currentTimeMillis()));
        ImageUtil.resize(imageFile.getAbsolutePath(), tempImage.getAbsolutePath(), IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, params.getImageQuality());
        params.setImageFile(tempImage);
        final Parser<BankCardResult> bankCardResultParser = new BankCardResultParser();
        this.getToken(new OnResultListener() {
            public void onResult(Object result) {
                HttpUtil.getInstance().post(OCR.this.urlAppendCommonParams(BANK_CARD_URL), params, bankCardResultParser, new OnResultListener<BankCardResult>() {
                    public void onResult(BankCardResult result) {
                        tempImage.delete();
                        if (listener != null) {
                            if (result.isRecCorrect()) {
                                listener.onResult(result);
                            } else {
                                OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, "recg bank card error " + result.getBankCardType());
                                listener.onError(error);
                            }
                        }
                    }

                    public void onError(OCRError error) {
                        tempImage.delete();
                        if (listener != null) {
                            listener.onError(error);
                        }

                    }
                });
            }

            public void onError(OCRError error) {
                listener.onError(error);
            }
        });
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void initWithToken(Context context, AccessToken token) {
        this.init(context);
        this.setAccessToken(token);
    }

    public void initAccessTokenWithAkSk(OnResultListener<AccessToken> listener, Context context, String ak, String sk) {
        this.authStatus = AUTHWITH_AKSK;
        this.ak = ak;
        this.sk = sk;
        this.init(context);
        AccessToken tokenFromCache = this.getByCache();
        if (tokenFromCache != null) {
            this.accessToken = tokenFromCache;
            listener.onResult(tokenFromCache);
            this.setLicense(tokenFromCache.getLic());
        } else {
            Throwable loadLibError = JniInterface.getLoadLibraryError();
            if (loadLibError != null) {
                SDKError e = new SDKError(283506, "Load jni so library error", loadLibError);
                listener.onError(e);
            } else {
                JniInterface jniInterface = new JniInterface();
                String hashSk = Util.md5(sk);
                byte[] buf = jniInterface.init(context, DeviceUtil.getDeviceInfo(context));
                String param = ak + ";" + hashSk + Base64.encodeToString(buf, 2);
                HttpUtil.getInstance().getAccessToken(listener, "https://verify.baidubce.com/verify/1.0/token/sk?sdkVersion=1_4_4", param);
            }

//            String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
//            String getAccessTokenUrl = authHost
//                    // 1. grant_type为固定参数
//                    + "grant_type=client_credentials"
//                    // 2. 官网获取的 API Key
//                    + "&client_id=" + ak
//                    // 3. 官网获取的 Secret Key
//                    + "&client_secret=" + sk;
//
//            HttpUtil.getInstance().getAccessToken(listener, getAccessTokenUrl, "");
        }
    }

    public String getLicense() {
        JniInterface jniInterface = new JniInterface();
        if (this.authStatus == AUTHWITH_LICENSE) {
            return jniInterface.getToken(this.context);
        } else if (this.authStatus == AUTHWITH_AKSK && this.license != null) {
            try {
                byte[] bin = Base64.decode(this.license, 0);
                String ret = jniInterface.getTokenFromLicense(this.context, bin, bin.length);
                return ret;
            } catch (Throwable var4) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void initAccessToken(OnResultListener<AccessToken> listener, Context context) {
        this.initAccessTokenImpl(listener, (String) null, context);
    }

    public void initAccessToken(OnResultListener<AccessToken> listener, String licenseFile, Context context) {
        this.initAccessTokenImpl(listener, licenseFile, context);
    }

    private void initAccessTokenImpl(OnResultListener<AccessToken> listener, String licenseFile, Context context) {
        this.authStatus = AUTHWITH_LICENSE;
        this.init(context);
        Throwable loadLibError = JniInterface.getLoadLibraryError();
        if (loadLibError != null) {
            SDKError e = new SDKError(SDKError.ErrorCode.LOAD_JNI_LIBRARY_ERROR, "Load jni so library error", loadLibError);
            listener.onError(e);
        } else {
            JniInterface jniInterface = new JniInterface();

            try {
                byte[] buf;
                if (licenseFile == null) {
                    buf = jniInterface.initWithBin(context, DeviceUtil.getDeviceInfo(context));
                } else {
                    buf = jniInterface.initWithBinLic(context, DeviceUtil.getDeviceInfo(context), licenseFile);
                }

                String param = Base64.encodeToString(buf, 2);
                AccessToken tokenFromCache = this.getByCache();
                if (tokenFromCache != null) {
                    this.accessToken = tokenFromCache;
                    listener.onResult(tokenFromCache);
                } else {
                    HttpUtil.getInstance().getAccessToken(listener, "https://verify.baidubce.com/verify/1.0/token/bin?sdkVersion=1_4_4", param);
                }
            } catch (OCRError var9) {
                listener.onError(var9);
            }

        }
    }

    private AccessToken getByCache() {
        if (!this.isAutoCacheToken) {
            return null;
        } else {
            SharedPreferences mSharedPreferences = this.context.getSharedPreferences(PREFRENCE_FILE_KEY, 0);
            String json = mSharedPreferences.getString(PREFRENCE_TOKENJSON_KEY, "");
            int type = mSharedPreferences.getInt(PREFRENCE_AUTH_TYPE, 0);
            if (type != this.authStatus) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.clear();
                editor.commit();
                return null;
            } else {
                AccessTokenParser parser = new AccessTokenParser();
                try {
                    AccessToken token = parser.parse(json);
                    long expireTime = mSharedPreferences.getLong("token_expire_time", 0L);
                    token.setExpireTime(expireTime);
                    this.authStatus = type;
                    return token;
                } catch (SDKError var8) {
                    return null;
                }
            }
        }
    }

    private synchronized boolean isTokenInvalid() {
        return null == this.accessToken || this.accessToken.hasExpired();
    }

    private void getToken(final OnResultListener listener) {
        if (this.isTokenInvalid()) {
            //清除本地accessToken
            SharedPreferences mSharedPreferences = this.context.getSharedPreferences(PREFRENCE_FILE_KEY, 0);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(PREFRENCE_AUTH_TYPE).apply();
            if (this.authStatus == AUTHWITH_AKSK) {
                this.initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
                    public void onResult(AccessToken result) {
                        OCR.this.setAccessToken(result);
                        listener.onResult(result);
                    }

                    public void onError(OCRError error) {
                        listener.onError(error);
                    }
                }, this.context, this.ak, this.sk);
            }

            if (this.authStatus == AUTHWITH_LICENSE) {
                this.initAccessToken(new OnResultListener<AccessToken>() {
                    public void onResult(AccessToken result) {
                        OCR.this.setAccessToken(result);
                        listener.onResult(result);
                    }

                    public void onError(OCRError error) {
                        listener.onError(error);
                    }
                }, this.context);
            }
        } else {
            listener.onResult(this.accessToken);
        }

    }

    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("access_token=").append(this.getAccessToken().getAccessToken());
        sb.append("&aipSdk=Android");
        sb.append("&aipSdkVersion=").append(OCR_SDK_VERSION);
        sb.append("&aipDevid=").append(DeviceUtil.getDeviceId(this.context));
        return sb.toString();
    }

    public void release() {
        try {
            HttpUtil.getInstance().release();
            if (this.crInst != null) {
                this.crInst.release();
                this.crInst = null;
            }
            if (this.context != null) {
                this.context = null;
            }
            if (instance != null) {
                instance = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
