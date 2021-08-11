package me.xuan.bdocr.sdk.model;

/**
 * Author: xuan
 * Created on 2019/10/23 14:54.
 * <p>
 * Describe:
 */
public class Location {
    private int left = -1;
    private int top = -1;
    private int width = -1;
    private int height = -1;

    public Location() {
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return this.top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
