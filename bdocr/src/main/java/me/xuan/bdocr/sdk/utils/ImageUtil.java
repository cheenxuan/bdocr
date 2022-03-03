package me.xuan.bdocr.sdk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
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
            int tempQuality = quality;
            Options options = new Options();
            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(inputPath, options);
            Matrix m = new Matrix();
            ExifInterface exif = new ExifInterface(inputPath);
            int rotation = exif.getAttributeInt("Orientation", 1);
            if (rotation != 0) {
                m.preRotate((float) ExifUtil.exifToDegrees(rotation));
            }

            int maxPreviewImageSize = Math.max(dstWidth, dstHeight);
            int size = Math.min(options.outWidth, options.outHeight);
            size = Math.min(size, maxPreviewImageSize);
            options.inSampleSize = calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = options.outWidth;
            options.inTargetDensity = size * options.inSampleSize;

            options.inJustDecodeBounds = false;
            Bitmap roughBitmap = BitmapFactory.decodeFile(inputPath, options);
            FileOutputStream out = new FileOutputStream(outputPath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                roughBitmap.compress(Bitmap.CompressFormat.JPEG, tempQuality, baos);
                while (baos.toByteArray().length / 1024 > 300) {
                    baos.reset();
                    roughBitmap.compress(Bitmap.CompressFormat.JPEG, tempQuality, baos);
                    tempQuality -= 5;
                }
                baos.writeTo(out);
                out.flush();
                if (roughBitmap != null && !roughBitmap.isRecycled()) {
                    roughBitmap.recycle();
                    roughBitmap = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    baos.close();
                    out.close();
                } catch (Exception var24) {
                    var24.printStackTrace();
                }
            }
        } catch (IOException var27) {
            var27.printStackTrace();
        }

    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
