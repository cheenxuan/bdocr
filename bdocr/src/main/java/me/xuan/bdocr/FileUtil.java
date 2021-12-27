/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package me.xuan.bdocr;

import android.content.Context;

import java.io.File;

public class FileUtil {
    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }

    public static File getSaveIdCardFrontFile(Context context) {
        File file = new File(context.getFilesDir(), "ID_CARD_front.jpg");
        return file;
    }

    public static File getSaveIdCardBackFile(Context context) {
//        String now = String.valueOf(System.currentTimeMillis());
        File file = new File(context.getFilesDir(), "ID_CARD_back.jpg");
        return file;
    }

    public static File getSaveBankCardFile(Context context) {
//        String now = String.valueOf(System.currentTimeMillis());
        File file = new File(context.getFilesDir(), "BANK_CARD.jpg");
        return file;
    }
}
