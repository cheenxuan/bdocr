package me.xuan.baiduocr;


import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import me.xuan.bdocr.sdk.OCR;
import me.xuan.bdocr.sdk.exception.OCRError;
import me.xuan.bdocr.ui.camera.CameraActivity;

/**
 * Author: xuan
 * Created on 2021/7/27 18:15.
 * <p>
 * Describe:
 */
public class ThirdRecgCardActivity extends CameraActivity {
    
    

    @Override
    public void showRecgLoading() {
        super.showRecgLoading();
    }

    @Override
    public void hideRecgLoading() {
        super.hideRecgLoading();
    }

    

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            OCR.getInstance().release();
        } catch (Exception e) {

        }
    }

    @Override
    public void showError( OCRError error) {
        super.showError(error);
        
        Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
    }
}