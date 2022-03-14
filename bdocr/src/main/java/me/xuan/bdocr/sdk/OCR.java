package me.xuan.bdocr.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.baidu.ocr.sdk.jni.JniInterface;

import java.io.File;

import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.exception.SDKError;
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

import static me.xuan.bdocr.sdk.exception.SDKError.ErrorCode.ACCESS_TOKEN_DATA_ERROR;

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
    private static final String PREFRENCE_FILE_KEY = "com.baidu.ocr.sdk";
    private static final String PREFRENCE_TOKENJSON_KEY = "token_json";
    private static final String PREFRENCE_EXPIRETIME_KEY = "token_expire_time";
    private static final String PREFRENCE_AUTH_TYPE = "token_auth_type";
    private static final int IMAGE_MAX_WIDTH = 2560;
    private static final int IMAGE_MAX_HEIGHT = 2560;
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
    private CrashReporterHandler crInst;
    private static volatile OCR instance;

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
            Class uiClass = Class.forName("com.baidu.ocr.ui.camera.CameraActivity");
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

        if (this.context == null) {
            OCRError error = new OCRError(ACCESS_TOKEN_DATA_ERROR, "识别失败，请重试");
            if (listener != null) {
                listener.onError(error);
            }
            return;
        }

        File imageFile = param.getImageFile();
        ImageUtil.resize(imageFile.getAbsolutePath(), imageFile.getAbsolutePath(), IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, param.getImageQuality());
        param.setImageFile(imageFile);
        final Parser<IDCardResult> idCardResultParser = new IDCardResultParser(param.getIdCardSide());
        this.getToken(new OnResultListener() {
            public void onResult(Object result) {
                HttpUtil.getInstance().post(OCR.this.urlAppendCommonParams(ID_CARD_URL), param, idCardResultParser, new OnResultListener<IDCardResult>() {
                    public void onResult(IDCardResult result) {

                        if ("normal".equals(result.getImageStatus())) {
                            if (listener != null) {
                                listener.onResult(result);
                            }
                        } else {
                            String errorStr;
                            if ("reversed_side".equals(result.getImageStatus())) {
                                errorStr = "身份证正反面颠倒";
                            } else if ("non_idcard".equals(result.getImageStatus())) {
                                errorStr = "上传的图片中不包含身份证";
                            } else if ("blurred".equals(result.getImageStatus())) {
                                errorStr = "身份证模糊";
                            } else if ("other_type_card".equals(result.getImageStatus())) {
                                errorStr = "其他类型证照";
                            } else if ("over_exposure".equals(result.getImageStatus())) {
                                errorStr = "身份证关键字段反光或过曝";
                            } else if ("over_dark".equals(result.getImageStatus())) {
                                errorStr = "身份证欠曝（亮度过低）";
                            } else {
                                errorStr = "身份证识别错误，请重试";
                            }

                            OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, errorStr);
                            if (listener != null) {
                                listener.onError(error);
                            }
                        }

//                        if(param.getDetectRisk()){
//                            if ("normal".equals(result.getRiskType()) && "normal".equals(result.getImageStatus())) {
//                                if (listener != null) {
//                                    listener.onResult(result);
//                                }
//                            } else {
//                                String errorStr;
//                                if ("copy".equals(result.getRiskType())) {
//                                    errorStr = "身份证复印件无法识别";
//                                } else if ("temporary".equals(result.getRiskType())) {
//                                    errorStr = "临时身份证无法识别";
//                                } else if ("screen".equals(result.getRiskType())) {
//                                    errorStr = "身份证翻拍无法识别";
//                                } else if ("reversed_side".equals(result.getImageStatus())) {
//                                    errorStr = "身份证正反面颠倒";
//                                } else if ("non_idcard".equals(result.getImageStatus())) {
//                                    errorStr = "上传的图片中不包含身份证";
//                                } else if ("blurred".equals(result.getImageStatus())) {
//                                    errorStr = "身份证模糊";
//                                } else if ("other_type_card".equals(result.getImageStatus())) {
//                                    errorStr = "其他类型证照";
//                                } else if ("over_exposure".equals(result.getImageStatus())) {
//                                    errorStr = "身份证关键字段反光或过曝";
//                                } else if ("over_dark".equals(result.getImageStatus())) {
//                                    errorStr = "身份证欠曝（亮度过低）";
//                                } else {
//                                    errorStr = "身份证识别错误，请重试";
//                                }
//
//                                OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, errorStr);
//                                if (listener != null) {
//                                    listener.onError(error);
//                                }
//                            }
//                        }else{
//                            if ("normal".equals(result.getImageStatus())) {
//                                if (listener != null) {
//                                    listener.onResult(result);
//                                }
//                            } else {
//                                String errorStr;
//                                if ("reversed_side".equals(result.getImageStatus())) {
//                                    errorStr = "身份证正反面颠倒";
//                                } else if ("non_idcard".equals(result.getImageStatus())) {
//                                    errorStr = "上传的图片中不包含身份证";
//                                } else if ("blurred".equals(result.getImageStatus())) {
//                                    errorStr = "身份证模糊";
//                                } else if ("other_type_card".equals(result.getImageStatus())) {
//                                    errorStr = "其他类型证照";
//                                } else if ("over_exposure".equals(result.getImageStatus())) {
//                                    errorStr = "身份证关键字段反光或过曝";
//                                } else if ("over_dark".equals(result.getImageStatus())) {
//                                    errorStr = "身份证欠曝（亮度过低）";
//                                } else {
//                                    errorStr = "身份证识别错误，请重试";
//                                }
//
//                                OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, errorStr);
//                                if (listener != null) {
//                                    listener.onError(error);
//                                }
//                            }
//                        }
                    }

                    public void onError(OCRError error) {
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
        if (this.context == null) {
            OCRError error = new OCRError(ACCESS_TOKEN_DATA_ERROR, "识别失败，请重试");
            if (listener != null) {
                listener.onError(error);
            }
            return;
        }
        File imageFile = params.getImageFile();
        ImageUtil.resize(imageFile.getAbsolutePath(), imageFile.getAbsolutePath(), IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, params.getImageQuality());
        params.setImageFile(imageFile);
        final Parser<BankCardResult> bankCardResultParser = new BankCardResultParser();
        this.getToken(new OnResultListener() {
            public void onResult(Object result) {
                HttpUtil.getInstance().post(OCR.this.urlAppendCommonParams(BANK_CARD_URL), params, bankCardResultParser, new OnResultListener<BankCardResult>() {
                    public void onResult(BankCardResult result) {
                        if (listener != null) {

                            if (BankCardResult.BankCardType.Unknown == result.getBankCardType()) {
                                String errorStr = "图片中不包含银行卡或图片模糊";
                                OCRError error = new OCRError(OCRError.ErrorCode.SERVICE_DATA_ERROR, errorStr);
                                listener.onError(error);
                            } else {
                                listener.onResult(result);
                            }
                        }
                    }

                    public void onError(OCRError error) {
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
//        System.out.println("OCR -> initAccessTokenWithAkSk -> " + tokenFromCache);
        if (tokenFromCache != null) {
            this.accessToken = tokenFromCache;
            listener.onResult(tokenFromCache);
            this.setLicense(tokenFromCache.getLic());
        } else {
            Throwable loadLibError = JniInterface.getLoadLibraryError();
            if (loadLibError != null) {
                SDKError e = new SDKError(SDKError.ErrorCode.LOAD_JNI_LIBRARY_ERROR, "Load jni so library error", loadLibError);
                listener.onError(e);
            } else {
                JniInterface jniInterface = new JniInterface();
                String hashSk = Util.md5(sk);
                byte[] buf = jniInterface.init(context, DeviceUtil.getDeviceInfo(context));
                String param = ak + ";" + hashSk + Base64.encodeToString(buf, 2);
                HttpUtil.getInstance().getAccessToken(listener, QUERY_TOKEN, param);
            }
        }
    }

    public void initAccessToken(OnResultListener<AccessToken> listener, Context context) {
        this.initAccessTokenImpl(listener, (String) null, context);
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
                    HttpUtil.getInstance().getAccessToken(listener, QUERY_TOKEN_BIN, param);
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
                    long expireTime = mSharedPreferences.getLong(PREFRENCE_EXPIRETIME_KEY, 0L);
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
        sb.append("&aipSdkVersion=").append("1_4_4");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
