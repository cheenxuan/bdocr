package me.xuan.bdocr.sdk.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: xuan
 * Created on 2019/10/23 14:38.
 * <p>
 * Describe:
 */
public class FileBase64Encoder {
    private InputStream inputStream;
    private byte[] buffer = new byte[24576];

    public FileBase64Encoder() {
    }

    public void setInputFile(File file) throws FileNotFoundException {
        this.inputStream = new FileInputStream(file);
    }

    public byte[] encode() {
        int readNumber;
        try {
            readNumber = this.inputStream.read(this.buffer);
            if (readNumber == -1) {
                this.closeInputStream();
                return null;
            }
        } catch (IOException var3) {
            this.closeInputStream();
            var3.printStackTrace();
            return null;
        }

        return Base64.encode(this.buffer, 0, readNumber, 2);
    }

    private void closeInputStream() {
        try {
            this.inputStream.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        } finally {
            this.inputStream = null;
        }

    }
}
