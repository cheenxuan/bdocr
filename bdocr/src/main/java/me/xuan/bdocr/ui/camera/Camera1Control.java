/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package me.xuan.bdocr.ui.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 5.0以下相机API的封装。
 */
@SuppressWarnings("deprecation")
public class Camera1Control implements ICameraControl {
//    private String TAG = this.getClass().getCanonicalName();

    private int displayOrientation = 0;
    private int cameraId = 0;
    private int flashMode;
    private AtomicBoolean takingPicture = new AtomicBoolean(false);
    private AtomicBoolean abortingScan = new AtomicBoolean(false);
    private Context context;
    private Camera camera;

    private Camera.Parameters parameters;
    private PermissionCallback permissionCallback;
    private Rect previewFrame = new Rect();

    private PreviewView previewView;
    private View displayView;
    private int rotation = 0;
    private int previewFrameCount = 0;
    private Camera.Size optSize;

    /*
     * 非扫描模式
     */
    private final int MODEL_NOSCAN = 0;

    private int detectType = MODEL_NOSCAN;

    public AtomicBoolean getAbortingScan() {
        return abortingScan;
    }

    @Override
    public void setDisplayOrientation(@CameraView.Orientation int displayOrientation) {
        this.displayOrientation = displayOrientation;
        switch (displayOrientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                rotation = 90;
                break;
            case CameraView.ORIENTATION_HORIZONTAL:
                rotation = 0;
                break;
            case CameraView.ORIENTATION_INVERT:
                rotation = 180;
                break;
            default:
                rotation = 0;
        }
        previewView.requestLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshPermission() {
        startPreview();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFlashMode(@FlashMode int flashMode) {
        if (this.flashMode == flashMode) {
            return;
        }
        this.flashMode = flashMode;
        updateFlashMode(flashMode);
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void start() {
        startPreview();
    }

    @Override
    public void stop() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            stopPreview();
            // 避免同步代码，为了先设置null后release
            Camera tempC = camera;
            camera = null;
            tempC.release();
            camera = null;
            buffer = null;
        }
    }

    private void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    @Override
    public void pause() {
        if (camera != null) {
            stopPreview();
        }
        setFlashMode(FLASH_MODE_OFF);
    }

    @Override
    public void resume() {
        takingPicture.set(false);
        if (camera == null) {
            openCamera();
        } else {
            previewView.textureView.setSurfaceTextureListener(surfaceTextureListener);
            if (previewView.textureView.isAvailable()) {
                startPreview();
            }
        }
    }

    @Override
    public View getDisplayView() {
        return displayView;
    }

    @Override
    public void takePicture(final OnTakePictureCallback onTakePictureCallback) {
        if (takingPicture.get()) {
            return;
        }
        switch (displayOrientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                parameters.setRotation(90);
                break;
            case CameraView.ORIENTATION_HORIZONTAL:
                parameters.setRotation(0);
                break;
            case CameraView.ORIENTATION_INVERT:
                parameters.setRotation(180);
                break;
        }
        try {
            Camera.Size picSize = getOptimalSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(picSize.width, picSize.height);
            camera.setParameters(parameters);
            takingPicture.set(true);
            cancelAutoFocus();
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            startPreview();
                            takingPicture.set(false);
                            if (onTakePictureCallback != null) {
                                onTakePictureCallback.onPictureTaken(data);
                            }
                        }
                    });
                }
            });

        } catch (RuntimeException e) {
            e.printStackTrace();
            startPreview();
            takingPicture.set(false);
        }
    }

    @Override
    public void setPermissionCallback(PermissionCallback callback) {
        this.permissionCallback = callback;
    }

    public Camera1Control(Context context) {
        this.context = context;
        previewView = new PreviewView(context);
        openCamera();
    }

    private void openCamera() {
        setupDisplayView();
    }

    private void setupDisplayView() {
        final TextureView textureView = new TextureView(context);
        previewView.textureView = textureView;
        previewView.setTextureView(textureView);
        displayView = previewView;
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private SurfaceTexture surfaceCache;

    private byte[] buffer = null;

    private void setPreviewCallbackImpl() {
        if (buffer == null) {
            buffer = new byte[displayView.getWidth()
                    * displayView.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8];
        }
    }

    private void initCamera() {
        try {
//            Log.d(TAG, "initCamera() camera = " + camera);
            if (camera == null) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        cameraId = i;
                    }
                }
                try {
                    camera = Camera.open(cameraId);
                } catch (Throwable e) {
//                    Log.d(TAG, "initCamera() open camera failed " + e.getMessage());
                    e.printStackTrace();
                    startPreview();
                    return;
                }
            }

            if (parameters == null) {
                parameters = camera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
            }
            opPreviewSize(previewView.getWidth(), previewView.getHeight());
            camera.setPreviewTexture(surfaceCache);
            setPreviewCallbackImpl();
            startPreview();
        } catch (IOException e) {
//            Log.d(TAG, "initCamera failed " + e.getMessage());
            e.printStackTrace();
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceCache = surface;
            initCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            opPreviewSize(previewView.getWidth(), previewView.getHeight());
            startPreview();
            setPreviewCallbackImpl();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            setPreviewCallbackImpl();
        }
    };

    // 开启预览
    private void startPreview() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionCallback != null) {
                permissionCallback.onRequestPermission();
                return;
            }
        }
        if (camera == null) {
            initCamera();
        } else {
            camera.startPreview();
            startAutoFocus();
        }
    }

    private void cancelAutoFocus() {
        camera.cancelAutoFocus();
        CameraThreadPool.cancelAutoFocusTimer();
    }

    private void startAutoFocus() {
        CameraThreadPool.createAutoFocusTimerTask(new Runnable() {
            @Override
            public void run() {
                synchronized (Camera1Control.this) {
                    if (camera != null && !takingPicture.get()) {
                        try {
                            camera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {
                                }
                            });
                        } catch (Throwable e) {
                            // startPreview是异步实现，可能在某些机器上前几次调用会autofocus failß
//                            Log.d(TAG, "startAutoFocus  onAutoFocus failed");
//                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void opPreviewSize(int width, @SuppressWarnings("unused") int height) {

        if (parameters != null && camera != null && width > 0) {
            try {
                optSize = getOptimalSize(parameters.getSupportedPreviewSizes());
                parameters.setPreviewSize(optSize.width, optSize.height);
                previewView.setRatio(1.0f * optSize.width / optSize.height);
                camera.stopPreview();
                camera.setDisplayOrientation(getSurfaceOrientation());
                camera.setParameters(parameters);
            } catch (RuntimeException e) {
//                Log.d(TAG, "opPreviewSize setParameters failed " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes) {
        List<Camera.Size> tempSizes = sizes;

        if (tempSizes == null || tempSizes.size() < 1) {
            return parameters.getSupportedPreviewSizes().get(0);
        }

        int width = previewView.textureView.getWidth();
        int height = previewView.textureView.getHeight();

        List<Camera.Size> candidates = new ArrayList<>();

        for (Camera.Size size : tempSizes) {

            if (size.width > 4095 || size.height > 4095) {
                continue;
            }
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size);
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        Collections.sort(tempSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                if (o1.width > o2.width && o1.height > o2.height) {
                    return 1;
                } else if (o1.width == o2.width && o1.height == o2.height) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        for (Camera.Size size : tempSizes) {
            if (size.width > width && size.height > height) {
                return size;
            }
        }
        
        return tempSizes.get(tempSizes.size() - 1);
//        return tempSizes.get(0);
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };

    private void updateFlashMode(int flashMode) {
        switch (flashMode) {
            case FLASH_MODE_TORCH:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
            case FLASH_MODE_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case ICameraControl.FLASH_MODE_AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            default:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
        camera.setParameters(parameters);
    }

    private int getSurfaceOrientation() {
        @CameraView.Orientation
        int orientation = displayOrientation;
        switch (orientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                return 90;
            case CameraView.ORIENTATION_HORIZONTAL:
                return 0;
            case CameraView.ORIENTATION_INVERT:
                return 180;
            default:
                return 90;
        }
    }

    /**
     * 有些相机匹配不到完美的比例。比如。我们的layout是4:3的。预览只有16:9
     * 的，如果直接显示图片会拉伸，变形。缩放的话，又有黑边。所以我们采取的策略
     * 是，等比例放大。这样预览的有一部分会超出屏幕。拍照后再进行裁剪处理。
     */
    private class PreviewView extends FrameLayout {

        private TextureView textureView;

        private float ratio = 0.75f;

        void setTextureView(TextureView textureView) {
            this.textureView = textureView;
            removeAllViews();
            addView(textureView);
        }

        void setRatio(float ratio) {
            this.ratio = ratio;
            requestLayout();
            relayout(getWidth(), getHeight());
        }

        public PreviewView(Context context) {
            super(context);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            relayout(w, h);
        }

        private void relayout(int w, int h) {
            int width = w;
            int height = h;
            if (w < h) {
                // 垂直模式，高度固定。
                height = (int) (width * ratio);
            } else {
                // 水平模式，宽度固定。
                width = (int) (height * ratio);
            }

            int l = (getWidth() - width) / 2;
            int t = (getHeight() - height) / 2;

            previewFrame.left = l;
            previewFrame.top = t;
            previewFrame.right = l + width;
            previewFrame.bottom = t + height;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            textureView.layout(previewFrame.left, previewFrame.top, previewFrame.right, previewFrame.bottom);
        }
    }

    @Override
    public Rect getPreviewFrame() {
        return previewFrame;
    }
}
