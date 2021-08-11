package me.xuan.bdocr.sdk.utils;

import android.util.Log;

/**
 * Author: xuan
 * Created on 2019/10/23 14:58.
 * <p>
 * Describe:
 */
public class ExifUtil {
    private static final String TAG = "CameraExif";

    public ExifUtil() {
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == 6) {
            return 90;
        } else if (exifOrientation == 3) {
            return 180;
        } else {
            return exifOrientation == 8 ? 270 : 0;
        }
    }

    public static int getOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        } else {
            int offset = 0;
            int length = 0;

            while(true) {
                while(true) {
                    int tag;
                    if (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
                        tag = jpeg[offset] & 0xFF;
                        if (tag == 0xFF) {
                            continue;
                        }

                        ++offset;
                        if (tag == 216 || tag == 1) {
                            continue;
                        }

                        if (tag != 217 && tag != 218) {
                            length = pack(jpeg, offset, 2, false);
                            if (length < 2 || offset + length > jpeg.length) {
                                Log.e("CameraExif", "Invalid length");
                                return 0;
                            }

                            if (tag != 225 || length < 8 || pack(jpeg, offset + 2, 4, false) != 1165519206 || pack(jpeg, offset + 6, 2, false) != 0) {
                                offset += length;
                                length = 0;
                                continue;
                            }

                            offset += 8;
                            length -= 8;
                        }
                    }

                    if (length > 8) {
                        tag = pack(jpeg, offset, 4, false);
                        if (tag != 1229531648 && tag != 1296891946) {
                            Log.e("CameraExif", "Invalid byte order");
                            return 0;
                        }

                        boolean littleEndian = tag == 1229531648;
                        int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
                        if (count < 10 || count > length) {
                            Log.e("CameraExif", "Invalid offset");
                            return 0;
                        }

                        offset += count;
                        length -= count;

                        for(count = pack(jpeg, offset - 2, 2, littleEndian); count-- > 0 && length >= 12; length -= 12) {
                            tag = pack(jpeg, offset, 2, littleEndian);
                            if (tag == 274) {
                                int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                                switch(orientation) {
                                    case 1:
                                        return 0;
                                    case 2:
                                    case 4:
                                    case 5:
                                    case 7:
                                    default:
                                        return 0;
                                    case 3:
                                        return 180;
                                    case 6:
                                        return 90;
                                    case 8:
                                        return 270;
                                }
                            }

                            offset += 12;
                        }
                    }

                    Log.i("CameraExif", "Orientation not found");
                    return 0;
                }
            }
        }
    }

    private static int pack(byte[] bytes, int offset, int length, boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value;
        for(value = 0; length-- > 0; offset += step) {
            value = value << 8 | bytes[offset] & 255;
        }

        return value;
    }
}
