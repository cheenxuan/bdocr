package me.xuan.bdocr.sdk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: xuan
 * Created on 2019/10/23 14:56.
 * <p>
 * Describe:
 */
public class ImageUtil {
    public ImageUtil() {
    }

    public static void resize(String inputPath, String outputPath, int dstWidth, int dstHeight) {
        resize(inputPath, outputPath, dstWidth, dstHeight, 80);
    }

    public static void resize(String inputPath, String outputPath, int dstWidth, int dstHeight, int quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(inputPath, options);
            int inWidth = options.outWidth;
            int inHeight = options.outHeight;
            Matrix m = new Matrix();
            ExifInterface exif = new ExifInterface(inputPath);
            int rotation = exif.getAttributeInt("Orientation", 1);
            if (rotation != 0) {
                m.preRotate((float)ExifUtil.exifToDegrees(rotation));
            }

            int maxPreviewImageSize = Math.max(dstWidth, dstHeight);
            int size = Math.min(options.outWidth, options.outHeight);
            size = Math.min(size, maxPreviewImageSize);
            options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = options.outWidth;
            options.inTargetDensity = size * options.inSampleSize;
            Bitmap roughBitmap = BitmapFactory.decodeFile(inputPath, options);
            FileOutputStream out = new FileOutputStream(outputPath);

            try {
                roughBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            } catch (Exception var25) {
                var25.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception var24) {
                    var24.printStackTrace();
                }

            }
        } catch (IOException var27) {
            var27.printStackTrace();
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;

            for(int halfWidth = width / 2; halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth; inSampleSize *= 2) {
                ;
            }
        }

        return inSampleSize;
    }
}
