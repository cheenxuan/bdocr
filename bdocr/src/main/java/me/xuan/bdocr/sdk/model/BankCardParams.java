package me.xuan.bdocr.sdk.model;

/**
 * Author: xuan
 * Created on 2019/10/23 15:10.
 * <p>
 * Describe:
 */
public class BankCardParams extends BaseImageParams {

    private int imageQuality = 20;

    public BankCardParams() {
    }

    public int getImageQuality() {
        return this.imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

}