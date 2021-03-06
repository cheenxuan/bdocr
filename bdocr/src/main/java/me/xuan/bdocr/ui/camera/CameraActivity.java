/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package me.xuan.bdocr.ui.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import me.xuan.bdocr.R;
import me.xuan.bdocr.ShowLoadingInterface;
import me.xuan.bdocr.sdk.OCR;
import me.xuan.bdocr.sdk.OnResultListener;
import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.sdk.model.BankCardParams;
import me.xuan.bdocr.sdk.model.BankCardResult;
import me.xuan.bdocr.sdk.model.IDCardParams;
import me.xuan.bdocr.sdk.model.IDCardResult;
import me.xuan.bdocr.sdk.utils.ImageUtil;
import me.xuan.bdocr.ui.crop.CropView;
import me.xuan.bdocr.ui.crop.FrameOverlayView;

public class CameraActivity extends FragmentActivity implements ShowLoadingInterface {

    public static final String KEY_OUTPUT_FILE_PATH = "outputFilePath";
    public static final String KEY_CONTENT_TYPE = "contentType";
    public static final String KEY_AUTO_RECOGNITION = "autorecogniton";
    public static final String KEY_REC_RESULT = "recresult";
    public static final String KEY_REC_RESULT_ES = "listResult";
    public static final String KEY_REC_RESULT_MAP = "mapResult";

    public static final String CONTENT_TYPE_GENERAL = "general";
    public static final String CONTENT_TYPE_ALBUM = "album";
    public static final String CONTENT_TYPE_ID_CARD_FRONT = "IDCardFront";
    public static final String CONTENT_TYPE_ID_CARD_BACK = "IDCardBack";
    public static final String CONTENT_TYPE_BANK_CARD = "bankCard";
    public static final String CONTENT_TYPE_PASSPORT = "passport";

    public static final String RESULT_ID_CARD_SIDE = "idCardSide";
    public static final String RESULT_ID_CARD_NAME = "name";
    public static final String RESULT_ID_CARD_GENDER = "gender";
    public static final String RESULT_ID_CARD_ETHNIC = "ethnic";
    public static final String RESULT_ID_CARD_BIRTHDAY = "birthday";
    public static final String RESULT_ID_CARD_ADDRESS = "address";
    public static final String RESULT_ID_CARD_NO = "idNumber";
    public static final String RESULT_ID_CARD_AUTHORITY = "issueAuthority";
    public static final String RESULT_ID_CARD_VAILD_DATE = "vaildDate";
    public static final String RESULT_IMAGE_PATH = "iamgePath";
    public static final String RESULT_IMAGE = "iamge";
    public static final String RESULT_IMAGE_TOP = "iamge_top";
    public static final String RESULT_IMAGE_LEFT = "iamge_left";
    public static final String RESULT_IMAGE_RIGHT = "iamge_right";
    public static final String RESULT_IMAGE_BOTTOM = "iamge_bottom";


    public static final String RESULT_BANK_CARD_NO = "bankCardNo";
    public static final String RESULT_BANK_NAME = "bankName";
    public static final String RESULT_BANK_CARD_TYPE = "bankCardType";
    public static final String RESULT_BANK_CARD_VAILD_DATE = "validDate";

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;

    private static final int IMAGE_MAX_WIDTH = 2560;
    private static final int IMAGE_MAX_HEIGHT = 2560;

    //????????????
    private long lastClickTime = 0L;
    // ??????????????????????????????1000ms
    private static final int FAST_CLICK_DELAY_TIME = 1000;

    private File outputFile;
    private String contentType;
    private Handler handler = new Handler(Looper.myLooper());
    private boolean isAutoRecg;
    private OCRCameraLayout takePictureContainer;
    private OCRCameraLayout cropContainer;
    private OCRCameraLayout confirmResultContainer;
    private ImageView lightButton;
    private CameraView cameraView;
    private ImageView displayImageView;
    private CropView cropView;
    private FrameOverlayView overlayView;
    private MaskView cropMaskView;
    private ImageView takePhotoBtn;
    private int cropType = 0;
    private TextView tvHint;
    private PermissionCallback permissionCallback = new PermissionCallback() {
        @Override
        public boolean onRequestPermission() {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bd_ocr_activity_camera);

        takePictureContainer = (OCRCameraLayout) findViewById(R.id.take_picture_container);
        confirmResultContainer = (OCRCameraLayout) findViewById(R.id.confirm_result_container);

        View idCardExamView = (View) findViewById(R.id.id_card_exam_container);
        View idCardBackExamView = (View) findViewById(R.id.id_card_back_exam_container);
        View bankCardExamView = (View) findViewById(R.id.bank_card_exam_container);

        contentType = getIntent().getStringExtra(KEY_CONTENT_TYPE);
        if (contentType.equals(CONTENT_TYPE_ID_CARD_FRONT)) {
            idCardExamView.setVisibility(View.VISIBLE);
        } else {
            idCardExamView.setVisibility(View.GONE);
        }

        if (contentType.equals(CONTENT_TYPE_ID_CARD_BACK)) {
            idCardBackExamView.setVisibility(View.VISIBLE);
        } else {
            idCardBackExamView.setVisibility(View.GONE);
        }

        if (contentType.equals(CONTENT_TYPE_BANK_CARD)) {
            bankCardExamView.setVisibility(View.VISIBLE);
        } else {
            bankCardExamView.setVisibility(View.GONE);
        }

        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.getCameraControl().setPermissionCallback(permissionCallback);
        lightButton = (ImageView) findViewById(R.id.light_button);
        lightButton.setOnClickListener(lightButtonOnClickListener);
        takePhotoBtn = (ImageView) findViewById(R.id.take_photo_button);
        findViewById(R.id.album_button).setOnClickListener(albumButtonOnClickListener);
        takePhotoBtn.setOnClickListener(takeButtonOnClickListener);

        // confirm result;
        displayImageView = (ImageView) findViewById(R.id.display_image_view);
        confirmResultContainer.findViewById(R.id.confirm_button).setOnClickListener(confirmButtonOnClickListener);
        confirmResultContainer.findViewById(R.id.cancel_button).setOnClickListener(confirmCancelButtonOnClickListener);
        findViewById(R.id.rotate_button).setOnClickListener(rotateButtonOnClickListener);

        cropView = (CropView) findViewById(R.id.crop_view);
        tvHint = (TextView) findViewById(R.id.tv_hint);
        cropContainer = (OCRCameraLayout) findViewById(R.id.crop_container);
        overlayView = (FrameOverlayView) findViewById(R.id.overlay_view);
        cropContainer.findViewById(R.id.confirm_button).setOnClickListener(cropConfirmButtonListener);
        cropMaskView = (MaskView) cropContainer.findViewById(R.id.crop_mask_view);
        cropContainer.findViewById(R.id.cancel_button).setOnClickListener(cropCancelButtonListener);

        setOrientation(getResources().getConfiguration());

        initParams();

        if (getIntent().getStringExtra(KEY_CONTENT_TYPE).equals(CONTENT_TYPE_ALBUM)) {
            openAlbum();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!contentType.equals(CONTENT_TYPE_ALBUM)) {
            cameraView.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!contentType.equals(CONTENT_TYPE_ALBUM)) {
                cameraView.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initParams() {
        String outputPath = getIntent().getStringExtra(KEY_OUTPUT_FILE_PATH);

        if (outputPath != null) {
            outputFile = new File(outputPath);
        }

        contentType = getIntent().getStringExtra(KEY_CONTENT_TYPE);
        if (contentType == null) {
            contentType = CONTENT_TYPE_GENERAL;
        }

        isAutoRecg = getIntent().getBooleanExtra(KEY_AUTO_RECOGNITION, false);

        int maskType;
        switch (contentType) {
            case CONTENT_TYPE_ID_CARD_FRONT:
                maskType = MaskView.MASK_TYPE_ID_CARD_FRONT;
                overlayView.setVisibility(View.INVISIBLE);
                cropMaskView.setVisibility(View.INVISIBLE);
                tvHint.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_ID_CARD_BACK:
                maskType = MaskView.MASK_TYPE_ID_CARD_BACK;
                overlayView.setVisibility(View.INVISIBLE);
                cropMaskView.setVisibility(View.INVISIBLE);
                tvHint.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_BANK_CARD:
                maskType = MaskView.MASK_TYPE_BANK_CARD;
                overlayView.setVisibility(View.INVISIBLE);
                cropMaskView.setVisibility(View.VISIBLE);
                tvHint.setVisibility(View.VISIBLE);
                break;
            case CONTENT_TYPE_PASSPORT:
                maskType = MaskView.MASK_TYPE_PASSPORT;
                overlayView.setVisibility(View.INVISIBLE);
                cropMaskView.setVisibility(View.INVISIBLE);
                tvHint.setVisibility(View.INVISIBLE);
                break;
            case CONTENT_TYPE_ALBUM:
            case CONTENT_TYPE_GENERAL:
            default:
                maskType = MaskView.MASK_TYPE_NONE;
                cropMaskView.setVisibility(View.INVISIBLE);
                tvHint.setVisibility(View.INVISIBLE);
                break;
        }

        cameraView.setMaskType(maskType, this);
        cropMaskView.setMaskType(maskType);
    }


    private void showTakePicture() {
        cameraView.getCameraControl().resume();
        updateFlashMode();
        takePictureContainer.setVisibility(View.VISIBLE);
        confirmResultContainer.setVisibility(View.INVISIBLE);
        cropContainer.setVisibility(View.INVISIBLE);
    }

    private void showCrop(int type) {
        this.cropType = type;
        cameraView.getCameraControl().pause();
        updateFlashMode();
        takePictureContainer.setVisibility(View.INVISIBLE);
        confirmResultContainer.setVisibility(View.INVISIBLE);
        cropContainer.setVisibility(View.VISIBLE);
    }

    private void showResultConfirm() {
        cameraView.getCameraControl().pause();
        updateFlashMode();
        takePictureContainer.setVisibility(View.INVISIBLE);
        confirmResultContainer.setVisibility(View.VISIBLE);
        cropContainer.setVisibility(View.INVISIBLE);
    }

    private void goOPenAlbum() {
        cameraView.getCameraControl().pause();
        updateFlashMode();
        openAlbum();
    }

    // take photo;
    private void updateFlashMode() {
        int flashMode = cameraView.getCameraControl().getFlashMode();
        if (flashMode == ICameraControl.FLASH_MODE_TORCH) {
            lightButton.setImageResource(R.drawable.bd_ocr_light_on);
        } else {
            lightButton.setImageResource(R.drawable.bd_ocr_light_off);
        }
    }

    private View.OnClickListener albumButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goOPenAlbum();
        }
    };

    private void openAlbum() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_EXTERNAL_STORAGE);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private View.OnClickListener lightButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cameraView.getCameraControl().getFlashMode() == ICameraControl.FLASH_MODE_OFF) {
                cameraView.getCameraControl().setFlashMode(ICameraControl.FLASH_MODE_TORCH);
            } else {
                cameraView.getCameraControl().setFlashMode(ICameraControl.FLASH_MODE_OFF);
            }
            updateFlashMode();
        }
    };

    private View.OnClickListener takeButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                cameraView.takePicture(outputFile, takePictureCallback);
                lastClickTime = System.currentTimeMillis();
            }

        }
    };

    private CameraView.OnTakePictureCallback takePictureCallback = new CameraView.OnTakePictureCallback() {
        @Override
        public void onPictureTaken(final Bitmap bitmap) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (bitmap != null) {
                        takePictureContainer.setVisibility(View.INVISIBLE);
                        displayImageView.setImageBitmap(bitmap);
                        showResultConfirm();
                    }
                }
            });
        }
    };

    private View.OnClickListener cropCancelButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!contentType.equals(CONTENT_TYPE_ALBUM)) {
                // ??????cropView??????bitmap;
                cropView.setFilePath(null);
                showTakePicture();
            } else {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }
    };

    private View.OnClickListener cropConfirmButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int maskType = cropMaskView.getMaskType();
            Rect rect;
            if (MaskView.MASK_TYPE_BANK_CARD == maskType) {
                rect = cropMaskView.getFrameRect();
            } else {
                rect = overlayView.getFrameRect();
            }
            Bitmap cropped = cropView.crop(rect);
            displayImageView.setImageBitmap(cropped);
            cropAndConfirm();
        }
    };

    private void cropAndConfirm() {
        cameraView.getCameraControl().pause();
        updateFlashMode();
        doConfirmResult();
    }

    private void doConfirmResult() {
        showRecgLoading();
        CameraThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    Bitmap bitmap = ((BitmapDrawable) displayImageView.getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //?????????????????????????????????
                if (isAutoRecg) {
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        //???????????????
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, outputFile.getAbsolutePath());
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        //???????????????
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, outputFile.getAbsolutePath());
                    } else if (CameraActivity.CONTENT_TYPE_BANK_CARD.equals(contentType)) {
                        //???????????????
                        recBankCard(outputFile.getAbsolutePath());
                    }
                } else {
                    try {
                        ImageUtil.resize(outputFile.getAbsolutePath(), outputFile.getAbsolutePath(), IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT, 97);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(RESULT_IMAGE_PATH, outputFile.getAbsolutePath());
                        setRecResult("", map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private View.OnClickListener confirmButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doConfirmResult();
        }
    };

    private View.OnClickListener confirmCancelButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            displayImageView.setImageBitmap(null);
            showTakePicture();

        }
    };

    private View.OnClickListener rotateButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cropView.rotate(90);
        }
    };

    private void recBankCard(String filePath) {
        BankCardParams param = new BankCardParams();
        //??????????????????
        param.setDetectDirection(true);
        param.setImageQuality(100);
        param.setImageFile(new File(filePath));
        OCR.getInstance().recognizeBankCard(param, new OnResultListener<BankCardResult>() {
            @Override
            public void onResult(BankCardResult result) {
                try {
                    if (TextUtils.isEmpty(outputFile.getAbsolutePath())) {
                        displayImageView.setImageBitmap(null);
                        showTakePicture();
                    } else {
                        try {
                            int degress = 0;
                            if (1 == result.getDirection()) {
                                degress = 90;
                            } else if (2 == result.getDirection()) {
                                degress = 180;
                            } else if (3 == result.getDirection()) {
                                degress = 270;
                            }

                            if (changeFileRotate(degress)) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(RESULT_BANK_CARD_NO, result.getBankCardNumber().replaceAll(" ", ""));
                                map.put(RESULT_BANK_NAME, result.getBankName());
                                map.put(RESULT_BANK_CARD_TYPE, result.getBankCardType().name());
                                map.put(RESULT_BANK_CARD_VAILD_DATE, "");
                                map.put(RESULT_IMAGE_PATH, outputFile.getAbsolutePath());
                                setRecResult("", map);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(OCRError error) {
                showError(error);
            }
        });
    }

    private boolean changeFileRotate(int degress) {
        try {
            Matrix matrix = new Matrix();
            if (degress > 0) {
                matrix.postRotate(degress);
            }

            Bitmap original = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createBitmap(original, 0, 0,
                    original.getWidth(), original.getHeight(), matrix, true);

            if (resizedBitmap != original && original != null && !original.isRecycled()) {
                original.recycle();
                original = null;
            }

            FileOutputStream fos = new FileOutputStream(outputFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            if (resizedBitmap != null && !resizedBitmap.isRecycled()) {
                resizedBitmap.recycle();
                resizedBitmap = null;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // ????????????????????????
        param.setIdCardSide(idCardSide);
        // ??????????????????
        param.setDetectDirection(true);
        // ??????????????????????????????0-100, ??????????????????????????????????????????????????? ????????????????????????20
        param.setImageQuality(95);
        //???????????????????????????(??????????????????????????????????????????????????????????????????????????????)??????
        param.setDetectRisk(false);
        //???????????????????????????(??????/????????????????????????????????????????????????/?????????)????????????
        param.setDetectQuality(true);
        //?????????????????????????????????
        param.setDetectCard(true);

        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                if (result != null) {
                    if (saveFile(result.getCardImage())) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(RESULT_ID_CARD_SIDE, result.getIdCardSide());
                        map.put(RESULT_ID_CARD_NAME, result.getName() == null ? null : result.getName().getWords());
                        map.put(RESULT_ID_CARD_GENDER, result.getGender() == null ? null : result.getGender().getWords());
                        map.put(RESULT_ID_CARD_ETHNIC, result.getEthnic() == null ? null : result.getEthnic().getWords());
                        map.put(RESULT_ID_CARD_BIRTHDAY, result.getBirthday() == null ? null : result.getBirthday().getWords());
                        map.put(RESULT_ID_CARD_ADDRESS, result.getAddress() == null ? null : result.getAddress().getWords());
                        map.put(RESULT_ID_CARD_NO, result.getIdNumber() == null ? null : result.getIdNumber().getWords());
                        map.put(RESULT_IMAGE_PATH, outputFile.getAbsolutePath());
                        map.put(RESULT_ID_CARD_AUTHORITY, result.getIssueAuthority() == null ? null : result.getIssueAuthority().getWords());
                        map.put(RESULT_ID_CARD_VAILD_DATE, result.getSignDate() == null ? null : result.getSignDate().getWords() + "-" + (result.getExpiryDate() == null ? null : result.getExpiryDate().getWords()));
                        setRecResult("", map);
                    }
                }
            }

            @Override
            public void onError(OCRError error) {
                showError(error);
            }
        });
    }

    private boolean saveFile(String base64Data) {
        try {

            if (TextUtils.isEmpty(base64Data)) {
                return false;
            }

            byte[] bytes = Base64.decode(base64Data, Base64.NO_WRAP);
            Bitmap original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            FileOutputStream fos = new FileOutputStream(outputFile);
            original.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            if (original != null && !original.isRecycled()) {
                original.recycle();
                original = null;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setRecResult(String result, HashMap<String, String> resultArr) {
        hideRecgLoading();
        Intent intent = new Intent();
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, contentType);
        intent.putExtra(CameraActivity.KEY_REC_RESULT, result);
        intent.putExtra(CameraActivity.KEY_REC_RESULT_MAP, resultArr);
        setResult(Activity.RESULT_OK, intent);
        CameraActivity.this.finish();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(contentURI, null, null, null, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setOrientation(newConfig);
    }

    private void setOrientation(Configuration newConfig) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int orientation;
        int cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = OCRCameraLayout.ORIENTATION_HORIZONTAL;
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    cameraViewOrientation = CameraView.ORIENTATION_HORIZONTAL;
                } else {
                    cameraViewOrientation = CameraView.ORIENTATION_INVERT;
                }
                break;
            default:
                orientation = OCRCameraLayout.ORIENTATION_PORTRAIT;
                cameraView.setOrientation(CameraView.ORIENTATION_PORTRAIT);
                break;
        }
        takePictureContainer.setOrientation(orientation);
        cameraView.setOrientation(cameraViewOrientation);
//        cropContainer.setOrientation(orientation);
        confirmResultContainer.setOrientation(orientation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Uri uri = data.getData();
                    cropView.setFilePath(getRealPathFromURI(uri));
                    showCrop(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (!contentType.equals(CONTENT_TYPE_ALBUM)) {
                    cameraView.getCameraControl().resume();
                } else {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraView.getCameraControl().refreshPermission();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PERMISSIONS_EXTERNAL_STORAGE:
            default:
                break;
        }
    }

    /**
     * ?????????????????????
     */
    private void doClear() {
        CameraThreadPool.cancelAutoFocusTimer();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.doClear();
    }

    @Override
    public void showRecgLoading() {
    }

    @Override
    public void hideRecgLoading() {
    }

    public String getContentType() {
        return this.contentType;
    }

    public void showError(OCRError error) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideRecgLoading();
            }
        }, 500);
    }


    /**
     * bitmap??????base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64??????bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
