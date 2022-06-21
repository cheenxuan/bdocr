package me.xuan.baiduocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



import me.xuan.bdocr.FileUtil;

import me.xuan.bdocr.sdk.OCR;
import me.xuan.bdocr.sdk.OnResultListener;
import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.model.AccessToken;
import me.xuan.bdocr.ui.camera.CameraActivity;


/**
 * Author: xuan
 * Created on 2021/7/27 18:05.
 * <p>
 * Describe:
 */
public class OcrManager {

    private static final String TAG = "OcrManager";
    private static final String ak = "LUTNjEHDfGS1Bxg1QuB3yZ1N";
    private static final String sk = "rFRj4I0mUIsFOaVUzc1gVsURW6j3UimD";

    public static void startBdIdCardOcr(final Activity activity, final boolean isBackSide, final int requestCode) {
        //初始化OCR
        initAccessTokenWithAkSk(activity.getApplicationContext(), new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                Log.i(TAG, "百度OCR初始化成功，access_token: " + token);
                //开始启动OCR
                Intent intent = new Intent(activity, ThirdRecgCardActivity.class);
                if (isBackSide) {
                    intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveIdCardFrontFile(activity.getApplicationContext()).getAbsolutePath());
                    intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
                } else {
                    intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveIdCardBackFile(activity.getApplicationContext()).getAbsolutePath());
                    intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
                }
                intent.putExtra(CameraActivity.KEY_AUTO_RECOGNITION, true);
                activity.startActivityForResult(intent, requestCode);
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();

            }
        });
    }

    public static void startBdBankCardOcr(final Activity activity, final int requestCode) {
        //初始化OCR
        initAccessTokenWithAkSk(activity.getApplicationContext(), new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                Log.i(TAG, "百度OCR初始化成功，access_token: " + token);
                //开始启动OCR
                Intent intent = new Intent(activity, ThirdRecgCardActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveBankCardFile(activity.getApplicationContext()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_BANK_CARD);
                intent.putExtra(CameraActivity.KEY_AUTO_RECOGNITION, true);
                activity.startActivityForResult(intent, requestCode);
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                LogUtil.d("获取token失败" + error.getMessage());
            }
        });
    }

    public static void startPhotoDefault(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ThirdRecgCardActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveFile(activity.getApplicationContext()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startAlnumDefault(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ThirdRecgCardActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveFile(activity.getApplicationContext()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ALBUM);
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * 用明文ak，sk初始化
     */
    private static void initAccessTokenWithAkSk(Context context, OnResultListener<AccessToken> listener) {
        OCR.getInstance().setAutoCacheToken(true);
        OCR.getInstance().initAccessTokenWithAkSk(listener, context, ak, sk);
    }


}